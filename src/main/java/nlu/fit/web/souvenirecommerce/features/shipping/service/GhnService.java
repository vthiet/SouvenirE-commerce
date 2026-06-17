package nlu.fit.web.souvenirecommerce.features.shipping.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import nlu.fit.web.souvenirecommerce.common.utils.ApplicationLoader;
import nlu.fit.web.souvenirecommerce.features.shipping.ShippingProvider;
import nlu.fit.web.souvenirecommerce.model.entity.Address;
import nlu.fit.web.souvenirecommerce.model.entity.Order;
import nlu.fit.web.souvenirecommerce.model.entity.OrderItem;
import nlu.fit.web.souvenirecommerce.model.enums.PaymentMethod;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GHN (Giao Hàng Nhanh) implementation of {@link ShippingProvider}.
 * All GHN-specific logic lives here; the rest of the application only knows
 * about the generic {@link ShippingProvider} interface.
 */
public class GhnService implements ShippingProvider {

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final String ghnMode = property("ghn.mode", "sandbox").toLowerCase();
    private final String baseUrl = switch (ghnMode) {
        case "live" -> property("ghn.base_url", "https://online-gateway.ghn.vn/shiip/public-api");
        case "simulation" -> property("ghn.sandbox_base_url", "https://dev-online-gateway.ghn.vn/shiip/public-api");
        default -> property("ghn.sandbox_base_url", "https://dev-online-gateway.ghn.vn/shiip/public-api");
    };
    private final String token = property("ghn.token", "");
    private final String shopId = property("ghn.shop_id", "");
    private final boolean simulation = "simulation".equals(ghnMode) || "mock".equals(ghnMode);
    private volatile List<GhNProvince> provinceCache;
    private volatile Map<Integer, List<GhNDistrict>> districtCacheByProvinceId;
    private volatile Map<Integer, List<GhNWard>> wardCacheByDistrictId;

    // -------------------------------------------------------------------------
    // ShippingProvider identity
    // -------------------------------------------------------------------------

    @Override
    public String getCode() {
        return "GHN";
    }

    @Override
    public String getName() {
        return "Giao Hàng Nhanh";
    }

    public boolean isSimulation() {
        return simulation;
    }

    // -------------------------------------------------------------------------
    // Location data (provinces / districts / wards)
    // -------------------------------------------------------------------------

    @Override
    public String getLocations(String type, String parentId) throws IOException, InterruptedException {
        if ("province".equals(type)) {
            return simulation ? simulationProvinces() : get("/master-data/province", null);
        }
        if ("district".equals(type)) {
            Integer provinceId = parseIntOrNull(parentId);
            if (provinceId == null) {
                throw new IOException("parentId (provinceId) is required for type=district");
            }
            return simulation
                    ? simulationDistricts(provinceId)
                    : get("/master-data/district", "province_id=" + provinceId);
        }
        if ("ward".equals(type)) {
            Integer districtId = parseIntOrNull(parentId);
            if (districtId == null) {
                throw new IOException("parentId (districtId) is required for type=ward");
            }
            return simulation
                    ? simulationWards(districtId)
                    : get("/master-data/ward", "district_id=" + districtId);
        }
        throw new IOException("Unknown location type: " + type);
    }

    // -------------------------------------------------------------------------
    // Fee calculation
    // -------------------------------------------------------------------------

    @Override
    public BigDecimal calculateFee(Address address) throws IOException, InterruptedException {
        if (address == null) {
            return BigDecimal.valueOf(30000);
        }
        return calculateFee(address.getCarrierDistrictId(), address.getCarrierWardCode());
    }

    public BigDecimal calculateFee(Integer toDistrictId, String toWardCode) throws IOException, InterruptedException {
        if (simulation) {
            return calculateSimulationFee(toDistrictId);
        }
        requireLiveShop();
        if (toDistrictId == null || toWardCode == null || toWardCode.isBlank()) {
            throw new IOException("Địa chỉ nhận hàng chưa có mã đơn vị vận chuyển.");
        }

        int fromDistrictId = Integer.parseInt(property("ghn.from_district_id", "1442"));
        int serviceId = getAvailableServiceId(fromDistrictId, toDistrictId);

        JsonObject body = new JsonObject();
        body.addProperty("from_district_id", fromDistrictId);
        body.addProperty("from_ward_code", property("ghn.from_ward_code", "20101"));
        body.addProperty("service_id", serviceId);
        body.addProperty("to_district_id", toDistrictId);
        body.addProperty("to_ward_code", toWardCode);
        body.addProperty("height", 10);
        body.addProperty("length", 20);
        body.addProperty("weight", 500);
        body.addProperty("width", 15);
        body.addProperty("insurance_value", 0);

        JsonObject data = post("/v2/shipping-order/fee", body).getAsJsonObject("data");
        int fee = data.get("total").getAsInt();
        return BigDecimal.valueOf(fee - (fee % 10));
    }

    // -------------------------------------------------------------------------
    // Shipment lifecycle
    // -------------------------------------------------------------------------

    @Override
    public ShipmentResult createShipment(Order order) throws IOException, InterruptedException {
        if (simulation) {
            return new ShipmentResult(
                    "SIM-" + order.getOrderCode(),
                    "ready_to_pick",
                    LocalDateTime.now().plusDays(3),
                    null,
                    LocalDateTime.now()
            );
        }
        requireLiveShop();

        Address address = order.getAddress();
        if (address == null || address.getCarrierDistrictId() == null
                || address.getCarrierWardCode() == null || address.getCarrierWardCode().isBlank()) {
            throw new IOException("Địa chỉ nhận hàng chưa có mã quận/huyện hoặc phường/xã của đơn vị vận chuyển.");
        }
        if (isBlank(resolveReceiverName(order))) {
            throw new IOException("Thiếu tên người nhận.");
        }
        if (isBlank(resolveReceiverPhone(order))) {
            throw new IOException("Thiếu số điện thoại người nhận.");
        }

        int fromDistrictId = Integer.parseInt(property("ghn.from_district_id", "1442"));
        String fromWardCode = property("ghn.from_ward_code", "20101");
        GhNSenderLocation sender = resolveSenderLocation(fromDistrictId, fromWardCode);
        GhNLocation destination = resolveDestinationLocation(address);
        int serviceId = getAvailableServiceId(fromDistrictId, destination.districtId());

        JsonObject body = new JsonObject();
        body.addProperty("payment_type_id", 1);
        body.addProperty("required_note", "KHONGCHOXEMHANG");
        body.addProperty("service_id", serviceId);
        body.addProperty("from_name", property("ghn.from_name", "INOLA"));
        body.addProperty("from_phone", property("ghn.from_phone", "0900000000"));
        body.addProperty("from_address", property("ghn.from_address", "Dia chi cua hang"));
        if (!isBlank(sender.provinceName())) {
            body.addProperty("from_province_name", sender.provinceName());
        }
        if (!isBlank(sender.districtName())) {
            body.addProperty("from_district_name", sender.districtName());
        }
        if (!isBlank(sender.wardName())) {
            body.addProperty("from_ward_name", sender.wardName());
        }
        body.addProperty("from_district_id", sender.districtId());
        body.addProperty("from_ward_code", sender.wardCode());
        body.addProperty("to_name", resolveReceiverName(order));
        body.addProperty("to_phone", resolveReceiverPhone(order));
        body.addProperty("to_address", address.getAddressDetail());
        if (!isBlank(destination.provinceName())) {
            body.addProperty("to_province_name", destination.provinceName());
        }
        if (!isBlank(destination.districtName())) {
            body.addProperty("to_district_name", destination.districtName());
        }
        if (!isBlank(destination.wardName())) {
            body.addProperty("to_ward_name", destination.wardName());
        }
        body.addProperty("to_ward_code", destination.wardCode());
        body.addProperty("to_district_id", destination.districtId());
        if (!isBlank(order.getNote())) {
            body.addProperty("note", order.getNote());
        }
        nlu.fit.web.souvenirecommerce.model.entity.PaymentTransaction ptx = new nlu.fit.web.souvenirecommerce.features.payment.repository.PaymentTransactionRepository().findByOrderId(order.getId()).orElse(null);
        body.addProperty("cod_amount", ptx != null
                && ptx.getMethod() == PaymentMethod.COD
                ? order.getTotalAmount().intValue()
                : 0);
        body.addProperty("content", "Đơn hàng " + order.getOrderCode());
        body.addProperty("weight", Math.max(500, order.getItems().size() * 500));
        body.addProperty("length", 20);
        body.addProperty("width", 15);
        body.addProperty("height", 10);
        body.addProperty("insurance_value", 0);
        body.addProperty("client_order_code", order.getOrderCode());

        JsonArray items = new JsonArray();
        for (OrderItem item : order.getItems()) {
            JsonObject jsonItem = new JsonObject();
            jsonItem.addProperty("name", resolveItemName(item));
            jsonItem.addProperty("code", resolveItemCode(item));
            jsonItem.addProperty("quantity", item.getQuantity());
            jsonItem.addProperty("price", item.getPriceAtPurchase() != null ? item.getPriceAtPurchase().intValue() : 0);
            jsonItem.addProperty("length", 20);
            jsonItem.addProperty("width", 15);
            jsonItem.addProperty("height", 10);
            jsonItem.addProperty("weight", 500);
            items.add(jsonItem);
        }
        body.add("items", items);

        JsonObject data = post("/v2/shipping-order/create", body).getAsJsonObject("data");
        return new ShipmentResult(
                stringValue(data, "order_code"),
                "ready_to_pick",
                parseDateTime(data, "expected_delivery_time"),
                null,
                LocalDateTime.now()
        );
    }

    @Override
    public ShipmentResult getShipmentStatus(String trackingCode, String currentStatus)
            throws IOException, InterruptedException {
        if (simulation) {
            String nextStatus = nextSimulationStatus(currentStatus);
            LocalDateTime finishDate = "delivered".equals(nextStatus) ? LocalDateTime.now() : null;
            return new ShipmentResult(
                    trackingCode, nextStatus,
                    LocalDateTime.now().plusDays(3),
                    finishDate,
                    LocalDateTime.now()
            );
        }
        requireLiveShop();

        JsonObject body = new JsonObject();
        body.addProperty("order_code", trackingCode);
        JsonObject data = post("/v2/shipping-order/detail", body).getAsJsonObject("data");
        return new ShipmentResult(
                stringValue(data, "order_code"),
                stringValue(data, "status"),
                parseDateTime(data, "leadtime"),
                parseDateTime(data, "finish_date"),
                parseDateTime(data, "updated_date")
        );
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private BigDecimal calculateSimulationFee(Integer toDistrictId) {
        if (toDistrictId == null) {
            return BigDecimal.valueOf(30000);
        }
        if (toDistrictId == 1442 || toDistrictId == 1451) {
            return BigDecimal.valueOf(22000);
        }
        if (toDistrictId >= 2000 && toDistrictId < 3000) {
            return BigDecimal.valueOf(35000);
        }
        return BigDecimal.valueOf(30000);
    }

    private int getAvailableServiceId(int fromDistrictId, int toDistrictId) throws IOException, InterruptedException {
        String configuredServiceId = property("ghn.service_id", "");
        if (!configuredServiceId.isBlank()) {
            return Integer.parseInt(configuredServiceId);
        }
        String response = get("/v2/shipping-order/available-services",
                "shop_id=" + shopId + "&from_district=" + fromDistrictId + "&to_district=" + toDistrictId);
        JsonArray services = dataArray(JsonParser.parseString(response).getAsJsonObject(), "data");
        if (services == null || services.isEmpty()) {
            throw new IOException("GHN: no available service found for the given districts");
        }
        return services.get(0).getAsJsonObject().get("service_id").getAsInt();
    }

    private void requireLiveShop() throws IOException {
        if (token.isBlank()) {
            throw new IOException("Thiếu GHN token trong cấu hình.");
        }
        if (shopId.isBlank()) {
            throw new IOException("Thiếu GHN shop_id trong cấu hình.");
        }
    }

    private JsonObject post(String path, JsonObject body) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .header("Token", token)
                .header("ShopId", shopId)
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("GHN error: " + response.body());
        }
        return JsonParser.parseString(response.body()).getAsJsonObject();
    }

    private String get(String path, String query) throws IOException, InterruptedException {
        String url = baseUrl + path + (query == null || query.isBlank() ? "" : "?" + query);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Token", token)
                .header("ShopId", shopId)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("GHN error: " + response.body());
        }
        return response.body();
    }

    // -------------------------------------------------------------------------
    // Simulation data
    // -------------------------------------------------------------------------

    private String simulationProvinces() {
        return """
                {"code":200,"message":"simulation","data":[
                  {"ProvinceID":202,"ProvinceName":"Hồ Chí Minh","NameExtension":["TP Hồ Chí Minh","HCM","Sài Gòn"]},
                  {"ProvinceID":201,"ProvinceName":"Hà Nội","NameExtension":["Ha Noi"]},
                  {"ProvinceID":203,"ProvinceName":"Đà Nẵng","NameExtension":["Da Nang"]}
                ]}
                """;
    }

    private String simulationDistricts(int provinceId) {
        if (provinceId == 201) {
            return """
                    {"code":200,"message":"simulation","data":[
                      {"DistrictID":2001,"DistrictName":"Quận Hoàn Kiếm","ProvinceID":201},
                      {"DistrictID":2002,"DistrictName":"Quận Ba Đình","ProvinceID":201}
                    ]}
                    """;
        }
        if (provinceId == 203) {
            return """
                    {"code":200,"message":"simulation","data":[
                      {"DistrictID":3001,"DistrictName":"Quận Hải Châu","ProvinceID":203},
                      {"DistrictID":3002,"DistrictName":"Quận Sơn Trà","ProvinceID":203}
                    ]}
                    """;
        }
        return """
                {"code":200,"message":"simulation","data":[
                  {"DistrictID":1442,"DistrictName":"Quận 1","ProvinceID":202},
                  {"DistrictID":1451,"DistrictName":"Thành phố Thủ Đức","ProvinceID":202},
                  {"DistrictID":1463,"DistrictName":"Quận 3","ProvinceID":202}
                ]}
                """;
    }

    private String simulationWards(int districtId) {
        if (districtId == 1451) {
            return """
                    {"code":200,"message":"simulation","data":[
                      {"WardCode":"20308","WardName":"Phường Linh Trung","DistrictID":1451},
                      {"WardCode":"20306","WardName":"Phường Bình Thọ","DistrictID":1451}
                    ]}
                    """;
        }
        return """
                {"code":200,"message":"simulation","data":[
                  {"WardCode":"20101","WardName":"Phường Bến Nghé","DistrictID":1442},
                  {"WardCode":"20102","WardName":"Phường Bến Thành","DistrictID":1442}
                ]}
                """;
    }

    private String nextSimulationStatus(String currentStatus) {
        if (currentStatus == null || currentStatus.isBlank()) {
            return "ready_to_pick";
        }
        return switch (currentStatus) {
            case "ready_to_pick" -> "picked";
            case "picked" -> "transporting";
            case "transporting" -> "delivering";
            case "delivering" -> "delivered";
            default -> currentStatus;
        };
    }

    private String stringValue(JsonObject object, String key) {
        return object == null || object.get(key) == null || object.get(key).isJsonNull()
                ? null
                : object.get(key).getAsString();
    }

    private LocalDateTime parseDateTime(JsonObject object, String key) {
        String value = stringValue(object, key);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value).toLocalDateTime();
        } catch (Exception ignored) {
            try {
                return LocalDateTime.parse(value);
            } catch (Exception ignoredAgain) {
                return null;
            }
        }
    }

    private Integer parseIntOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String property(String key, String fallback) {
        String value = ApplicationLoader.get(key);
        return value == null || value.isBlank() || value.startsWith("YOUR_") ? fallback : value.trim();
    }

    private String resolveReceiverName(Order order) {
        if (order == null) {
            return null;
        }
        Address address = order.getAddress();
        if (address != null && !isBlank(address.getReceiverName())) {
            return address.getReceiverName().trim();
        }
        if (order.getUser() != null && !isBlank(order.getUser().getFullName())) {
            return order.getUser().getFullName().trim();
        }
        return null;
    }

    private String resolveReceiverPhone(Order order) {
        if (order == null) {
            return null;
        }
        Address address = order.getAddress();
        if (address != null && !isBlank(address.getReceiverPhone())) {
            return address.getReceiverPhone().trim();
        }
        if (order.getUser() != null && !isBlank(order.getUser().getPhone())) {
            return order.getUser().getPhone().trim();
        }
        return null;
    }

    private String resolveItemName(OrderItem item) {
        if (item == null) {
            return "San pham";
        }
        if (!isBlank(item.getProductName())) {
            return item.getProductName().trim();
        }
        if (item.getProduct() != null && !isBlank(item.getProduct().getName())) {
            return item.getProduct().getName().trim();
        }
        return "San pham";
    }

    private String resolveItemCode(OrderItem item) {
        if (item == null) {
            return "0";
        }
        if (item.getProduct() != null && item.getProduct().getId() != null) {
            return String.valueOf(item.getProduct().getId());
        }
        if (!isBlank(item.getProductName())) {
            return item.getProductName().trim();
        }
        return "0";
    }

    private JsonArray dataArray(JsonObject object, String key) {
        if (object == null || key == null || !object.has(key) || object.get(key) == null || object.get(key).isJsonNull()) {
            return new JsonArray();
        }
        if (!object.get(key).isJsonArray()) {
            return new JsonArray();
        }
        return object.getAsJsonArray(key);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private GhNLocation resolveDestinationLocation(Address address) throws IOException, InterruptedException {
        GhNProvince province = resolveProvince(address);
        if (province == null) {
            throw new IOException("Không thể xác định mã tỉnh/thành phố GHN cho địa chỉ giao hàng.");
        }

        GhNDistrict district = resolveDistrict(address, province);
        if (district == null) {
            throw new IOException("Không thể xác định mã quận/huyện GHN cho địa chỉ giao hàng.");
        }

        GhNWard ward = resolveWard(address, district);
        if (ward == null) {
            throw new IOException("Không thể xác định mã phường/xã GHN cho địa chỉ giao hàng.");
        }

        return new GhNLocation(province.name(), district.name(), district.id(), ward.code(), ward.name());
    }

    private GhNSenderLocation resolveSenderLocation(int fromDistrictId, String fromWardCode) throws IOException, InterruptedException {
        GhNDistrict district = null;
        GhNProvince province = null;
        for (GhNProvince candidateProvince : loadProvinces()) {
            GhNDistrict candidateDistrict = findDistrictById(candidateProvince.id(), fromDistrictId);
            if (candidateDistrict != null) {
                district = candidateDistrict;
                province = candidateProvince;
                break;
            }
        }
        if (district == null) {
            throw new IOException("Không thể xác định mã quận/huyện GHN của kho gửi hàng.");
        }

        GhNWard ward = findWardByCode(fromDistrictId, fromWardCode);
        if (ward == null) {
            throw new IOException("Không thể xác định mã phường/xã GHN của kho gửi hàng.");
        }

        if (province == null) {
            province = findProvinceById(district.provinceId());
        }
        if (province == null) {
            throw new IOException("Không thể xác định mã tỉnh/thành phố GHN của kho gửi hàng.");
        }

        return new GhNSenderLocation(province.name(), district.name(), ward.name(), district.id(), ward.code());
    }

    private GhNProvince resolveProvince(Address address) throws IOException, InterruptedException {
        String localProvinceName = firstNonBlank(address.getProvince(), address.getCity());
        GhNProvince matched = findProvinceByName(localProvinceName);
        if (matched != null) {
            return matched;
        }
        Integer carrierProvinceId = address.getCarrierProvinceId();
        if (carrierProvinceId != null) {
            return findProvinceById(carrierProvinceId);
        }
        return null;
    }

    private GhNDistrict resolveDistrict(Address address, GhNProvince province) throws IOException, InterruptedException {
        String localDistrictName = address.getDistrict();
        GhNDistrict matched = findDistrictByName(province.id(), localDistrictName);
        if (matched != null) {
            return matched;
        }
        Integer carrierDistrictId = address.getCarrierDistrictId();
        if (carrierDistrictId != null) {
            return findDistrictById(province.id(), carrierDistrictId);
        }
        return null;
    }

    private GhNWard resolveWard(Address address, GhNDistrict district) throws IOException, InterruptedException {
        String localWardName = address.getWard();
        GhNWard matched = findWardByName(district.id(), localWardName);
        if (matched != null) {
            return matched;
        }
        String carrierWardCode = address.getCarrierWardCode();
        if (!isBlank(carrierWardCode)) {
            return findWardByCode(district.id(), carrierWardCode);
        }
        return null;
    }

    private String normalizeLocationName(String value) {
        if (value == null) {
            return "";
        }
        return value.trim()
                .toLowerCase()
                .replace("phường ", "")
                .replace("xã ", "")
                .replace("quận ", "")
                .replace("thành phố ", "")
                .replace("tp. ", "")
                .replace("tp ", "")
                .replace("tỉnh ", "")
                .replace(".", "")
                .replace(",", "")
                .replaceAll("\\s+", " ");
    }

    private GhNProvince findProvinceByName(String provinceName) throws IOException, InterruptedException {
        if (isBlank(provinceName)) {
            return null;
        }
        String normalized = normalizeLocationName(provinceName);
        for (GhNProvince province : loadProvinces()) {
            if (normalized.equals(normalizeLocationName(province.name()))) {
                return province;
            }
        }
        return null;
    }

    private GhNProvince findProvinceById(Integer provinceId) throws IOException, InterruptedException {
        if (provinceId == null) {
            return null;
        }
        for (GhNProvince province : loadProvinces()) {
            if (provinceId.equals(province.id())) {
                return province;
            }
        }
        return null;
    }

    private GhNDistrict findDistrictByName(Integer provinceId, String districtName) throws IOException, InterruptedException {
        if (provinceId == null || isBlank(districtName)) {
            return null;
        }
        String normalized = normalizeLocationName(districtName);
        for (GhNDistrict district : loadDistricts(provinceId)) {
            if (normalized.equals(normalizeLocationName(district.name()))) {
                return district;
            }
        }
        return null;
    }

    private GhNDistrict findDistrictById(Integer provinceId, Integer districtId) throws IOException, InterruptedException {
        if (provinceId == null || districtId == null) {
            return null;
        }
        for (GhNDistrict district : loadDistricts(provinceId)) {
            if (districtId.equals(district.id())) {
                return district;
            }
        }
        return null;
    }

    private GhNWard findWardByName(Integer districtId, String wardName) throws IOException, InterruptedException {
        if (districtId == null || isBlank(wardName)) {
            return null;
        }
        String normalized = normalizeLocationName(wardName);
        for (GhNWard ward : loadWards(districtId)) {
            if (normalized.equals(normalizeLocationName(ward.name()))) {
                return ward;
            }
        }
        return null;
    }

    private GhNWard findWardByCode(Integer districtId, String wardCode) throws IOException, InterruptedException {
        if (districtId == null || isBlank(wardCode)) {
            return null;
        }
        for (GhNWard ward : loadWards(districtId)) {
            if (wardCode.equals(ward.code())) {
                return ward;
            }
        }
        return null;
    }

    private List<GhNProvince> loadProvinces() throws IOException, InterruptedException {
        if (provinceCache != null) {
            return provinceCache;
        }
        synchronized (this) {
            if (provinceCache == null) {
                List<GhNProvince> loaded = new java.util.ArrayList<>();
                JsonObject response = JsonParser.parseString(get("/master-data/province", null)).getAsJsonObject();
                JsonArray provinces = dataArray(response, "data");
                if (provinces != null) {
                    for (int i = 0; i < provinces.size(); i++) {
                        JsonObject province = provinces.get(i).getAsJsonObject();
                        Integer id = province.get("ProvinceID").getAsInt();
                        String name = stringValue(province, "ProvinceName");
                        if (id != null && !isBlank(name)) {
                            loaded.add(new GhNProvince(id, name));
                        }
                    }
                }
                provinceCache = List.copyOf(loaded);
            }
        }
        return provinceCache;
    }

    private List<GhNDistrict> loadDistricts(Integer provinceId) throws IOException, InterruptedException {
        if (provinceId == null) {
            return List.of();
        }
        if (districtCacheByProvinceId == null) {
            synchronized (this) {
                if (districtCacheByProvinceId == null) {
                    districtCacheByProvinceId = new ConcurrentHashMap<>();
                }
            }
        }
        List<GhNDistrict> cached = districtCacheByProvinceId.get(provinceId);
        if (cached != null) {
            return cached;
        }

        synchronized (districtCacheByProvinceId) {
            cached = districtCacheByProvinceId.get(provinceId);
            if (cached == null) {
                List<GhNDistrict> loaded = new java.util.ArrayList<>();
                JsonObject response = JsonParser.parseString(get("/master-data/district", "province_id=" + provinceId))
                        .getAsJsonObject();
                JsonArray districts = dataArray(response, "data");
                if (districts != null) {
                    for (int i = 0; i < districts.size(); i++) {
                        JsonObject district = districts.get(i).getAsJsonObject();
                        Integer id = district.get("DistrictID").getAsInt();
                        Integer returnedProvinceId = district.get("ProvinceID") != null ? district.get("ProvinceID").getAsInt() : provinceId;
                        String name = stringValue(district, "DistrictName");
                        if (id != null && !isBlank(name)) {
                            loaded.add(new GhNDistrict(id, returnedProvinceId, name));
                        }
                    }
                }
                cached = List.copyOf(loaded);
                districtCacheByProvinceId.put(provinceId, cached);
            }
        }
        return cached;
    }

    private List<GhNWard> loadWards(Integer districtId) throws IOException, InterruptedException {
        if (districtId == null) {
            return List.of();
        }
        if (wardCacheByDistrictId == null) {
            synchronized (this) {
                if (wardCacheByDistrictId == null) {
                    wardCacheByDistrictId = new ConcurrentHashMap<>();
                }
            }
        }
        List<GhNWard> cached = wardCacheByDistrictId.get(districtId);
        if (cached != null) {
            return cached;
        }

        synchronized (wardCacheByDistrictId) {
            cached = wardCacheByDistrictId.get(districtId);
            if (cached == null) {
                List<GhNWard> loaded = new java.util.ArrayList<>();
                JsonObject response = JsonParser.parseString(get("/master-data/ward", "district_id=" + districtId))
                        .getAsJsonObject();
                JsonArray wards = dataArray(response, "data");
                if (wards != null) {
                    for (int i = 0; i < wards.size(); i++) {
                        JsonObject ward = wards.get(i).getAsJsonObject();
                        String code = stringValue(ward, "WardCode");
                        Integer returnedDistrictId = ward.get("DistrictID") != null ? ward.get("DistrictID").getAsInt() : districtId;
                        String name = stringValue(ward, "WardName");
                        if (!isBlank(code) && !isBlank(name)) {
                            loaded.add(new GhNWard(code, returnedDistrictId, name));
                        }
                    }
                }
                cached = List.copyOf(loaded);
                wardCacheByDistrictId.put(districtId, cached);
            }
        }
        return cached;
    }

    private String firstNonBlank(String first, String second) {
        if (!isBlank(first)) {
            return first.trim();
        }
        if (!isBlank(second)) {
            return second.trim();
        }
        return null;
    }

    private record GhNProvince(Integer id, String name) {}

    private record GhNDistrict(Integer id, Integer provinceId, String name) {}

    private record GhNWard(String code, Integer districtId, String name) {}

    private record GhNSenderLocation(String provinceName, String districtName, String wardName, Integer districtId, String wardCode) {}

    private record GhNLocation(String provinceName, String districtName, Integer districtId, String wardCode, String wardName) {}
}
