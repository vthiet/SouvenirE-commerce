package nlu.fit.web.souvenirecommerce.features.shipping.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
import java.util.Locale;

/**
 * GHN (Giao Hàng Nhanh) implementation of {@link ShippingProvider}.
 * All GHN-specific logic lives here; the rest of the application only knows
 * about the generic {@link ShippingProvider} interface.
 */
public class GhnService implements ShippingProvider {

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    private final String mode = normalizedMode(property("ghn.mode", "simulation"));
    private final boolean simulation = "simulation".equals(mode);
    private final String baseUrl = resolveBaseUrl();
    private final String token = property("ghn.token", "");
    private final String shopId = property("ghn.shop_id", "");

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

    public String getProvinces() throws IOException, InterruptedException {
        if (simulation) {
            return simulationProvinces();
        }
        return get("/master-data/province", null, false);
    }

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
        return get("/master-data/district", "province_id=" + provinceId, false);
    }

    // -------------------------------------------------------------------------
    // Fee calculation
    // -------------------------------------------------------------------------

    @Override
    public BigDecimal calculateFee(Address address) throws IOException, InterruptedException {
        if (address == null) {
            return BigDecimal.valueOf(30000);
        }
        return get("/master-data/ward", "district_id=" + districtId, false);
    }

    public BigDecimal calculateFee(Integer toDistrictId, String toWardCode) throws IOException, InterruptedException {
        if (simulation) {
            return calculateSimulationFee(toDistrictId);
        }
        requireApiCredentials();
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

        JsonObject data = post("/v2/shipping-order/fee", body, true).getAsJsonObject("data");
        int fee = data.get("total").getAsInt();
        return BigDecimal.valueOf(fee - (fee % 10));
    }

    @Override
    public String getName() {
        return "GHN";
    }

    @Override
    public BigDecimal calculateFee(Address address) throws IOException, InterruptedException {
        if (address == null) {
            return BigDecimal.valueOf(30000);
        }
        return calculateFee(resolveGhnDistrictId(address), resolveGhnWardCode(address));
    }

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
        requireApiCredentials();

        Address address = order.getAddress();
        Integer toDistrictId = resolveGhnDistrictId(address);
        String toWardCode = resolveGhnWardCode(address);
        if (address == null || toDistrictId == null || toWardCode == null || toWardCode.isBlank()) {
            throw new IOException("Địa chỉ nhận hàng chưa có mã quận/huyện hoặc phường/xã GHN.");
        }

        int fromDistrictId = Integer.parseInt(property("ghn.from_district_id", "1442"));
        AvailableService availableService = getAvailableService(fromDistrictId, toDistrictId);

        String toProvinceName = resolveProvinceName(address);
        String toDistrictName = resolveDistrictName(address);
        String toWardName = resolveWardName(address);
        String toPhone = resolveRecipientPhone(order);
        String toName = firstNonBlank(address.getReceiverName(), order.getUser() == null ? null : order.getUser().getFullName());
        String toAddress = buildRecipientAddress(address, toWardName, toDistrictName, toProvinceName);
        if (isBlank(toProvinceName) || isBlank(toDistrictName) || isBlank(toWardName)) {
            throw new IOException("Địa chỉ nhận hàng chưa có đủ tên tỉnh/thành, quận/huyện hoặc phường/xã.");
        }

        JsonObject body = new JsonObject();
        body.addProperty("payment_type_id", 1);
        body.addProperty("required_note", "KHONGCHOXEMHANG");
        // GHN sandbox is most reliable when both fields are sent.
        body.addProperty("service_id", availableService.serviceId());
        body.addProperty("service_type_id", availableService.serviceTypeId());
        body.addProperty("from_name", property("ghn.from_name", "INOLA"));
        body.addProperty("from_phone", property("ghn.from_phone", "0900000000"));
        body.addProperty("from_address", property("ghn.from_address", "Dia chi cua hang"));
        body.addProperty("from_ward_name", property("ghn.from_ward_name", "Phuong Ben Nghe"));
        body.addProperty("from_district_name", property("ghn.from_district_name", "Quan 1"));
        body.addProperty("from_province_name", property("ghn.from_province_name", "Ho Chi Minh"));
        body.addProperty("to_name", toName);
        body.addProperty("to_phone", toPhone);
        body.addProperty("to_address", toAddress);
        body.addProperty("to_ward_code", toWardCode);
        body.addProperty("to_district_id", toDistrictId);
        body.addProperty("to_ward_name", toWardName);
        body.addProperty("to_district_name", toDistrictName);
        body.addProperty("to_province_name", toProvinceName);
        body.addProperty("cod_amount", order.getPaymentTransaction() != null
                && order.getPaymentTransaction().getMethod() == PaymentMethod.COD
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
            jsonItem.addProperty("name", item.getProductName());
            jsonItem.addProperty("code", String.valueOf(item.getProduct().getId()));
            jsonItem.addProperty("quantity", item.getQuantity());
            jsonItem.addProperty("price", item.getPriceAtPurchase().intValue());
            jsonItem.addProperty("length", 20);
            jsonItem.addProperty("width", 15);
            jsonItem.addProperty("height", 10);
            jsonItem.addProperty("weight", 500);
            items.add(jsonItem);
        }
        body.add("items", items);

        JsonObject data = post("/v2/shipping-order/create", body, true).getAsJsonObject("data");
        return new ShippingOrderResult(
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
        requireApiCredentials();

        JsonObject body = new JsonObject();
        body.addProperty("order_code", orderCode);
        JsonObject data = post("/v2/shipping-order/detail", body, false).getAsJsonObject("data");
        return new ShippingOrderResult(
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
        return getAvailableService(fromDistrictId, toDistrictId).serviceId();
    }

    private AvailableService getAvailableService(int fromDistrictId, int toDistrictId) throws IOException, InterruptedException {
        String configuredServiceId = property("ghn.service_id", "");

        JsonObject body = new JsonObject();
        body.addProperty("shop_id", parseRequiredShopId());
        body.addProperty("from_district", fromDistrictId);
        body.addProperty("to_district", toDistrictId);

        JsonObject response = post("/v2/shipping-order/available-services", body, false);
        JsonArray services = response.has("data") && response.get("data").isJsonArray()
                ? response.getAsJsonArray("data")
                : null;
        if (services == null || services.isEmpty()) {
            throw new IOException("GHN: no available service found for the given districts");
        }

        if (!configuredServiceId.isBlank()) {
            int configuredId = Integer.parseInt(configuredServiceId);
            for (JsonElement serviceElement : services) {
                JsonObject service = serviceElement.getAsJsonObject();
                Integer serviceId = extractInt(service, "service_id");
                if (serviceId != null && serviceId == configuredId) {
                    Integer serviceTypeId = extractInt(service, "service_type_id");
                    if (serviceTypeId == null) {
                        throw new IOException("GHN service response thiếu service_type_id.");
                    }
                    return new AvailableService(serviceId, serviceTypeId);
                }
            }
            throw new IOException("GHN service_id không hợp lệ trong cấu hình.");
        }

        JsonObject service = services.get(0).getAsJsonObject();
        Integer serviceId = extractInt(service, "service_id");
        Integer serviceTypeId = extractInt(service, "service_type_id");
        if (serviceId == null || serviceTypeId == null) {
            throw new IOException("GHN service response thiếu service_id hoặc service_type_id.");
        }
        return new AvailableService(serviceId, serviceTypeId);
    }

    private void requireApiCredentials() throws IOException {
        if (token.isBlank()) {
            throw new IOException("Thiếu GHN token trong cấu hình.");
        }
        if (shopId.isBlank()) {
            throw new IOException("Thiếu GHN shop_id trong cấu hình.");
        }
    }

    private JsonObject post(String path, JsonObject body, boolean includeShopIdHeader) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .header("Token", token)
                .header("Accept", "application/json");
        if (includeShopIdHeader) {
            builder.header("ShopId", shopId);
        }
        HttpRequest request = builder
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return readGhnJson(response);
    }

    private String get(String path, String query, boolean includeShopIdHeader) throws IOException, InterruptedException {
        String url = baseUrl + path + (query == null || query.isBlank() ? "" : "?" + query);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Token", token)
                .header("Accept", "application/json");
        if (includeShopIdHeader) {
            builder.header("ShopId", shopId);
        }
        HttpRequest request = builder.GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return readGhnJson(response).toString();
    }

    private int parseRequiredShopId() throws IOException {
        try {
            return Integer.parseInt(shopId);
        } catch (NumberFormatException e) {
            throw new IOException("GHN shop_id không hợp lệ trong cấu hình.");
        }
    }

    private JsonObject readGhnJson(HttpResponse<String> response) throws IOException {
        String body = response.body();
        JsonObject json = tryParseJsonObject(body);

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException(formatGhnError(response.statusCode(), json, body));
        }

        if (json == null) {
            throw new IOException("GHN trả về phản hồi không hợp lệ.");
        }

        Integer apiCode = extractInt(json, "code");
        if (apiCode != null && apiCode != 200) {
            throw new IOException(formatGhnError(response.statusCode(), json, body));
        }

        return json;
    }

    private JsonObject tryParseJsonObject(String body) {
        if (body == null || body.isBlank()) {
            return null;
        }
        try {
            JsonElement element = JsonParser.parseString(body);
            return element != null && element.isJsonObject() ? element.getAsJsonObject() : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private String formatGhnError(int httpStatus, JsonObject json, String rawBody) {
        String message = json == null ? null : stringValue(json, "message");
        String codeMessage = json == null ? null : stringValue(json, "code_message");
        String codeMessageValue = json == null ? null : stringValue(json, "code_message_value");

        if (httpStatus == 401
                || containsIgnoreCase(message, "Token is not valid")
                || containsIgnoreCase(rawBody, "\"code\":401")) {
            return "GHN token không hợp lệ cho môi trường hiện tại (" + mode + "). "
                    + "Nếu bạn đang dùng token sandbox, hãy đặt `ghn.mode=sandbox`; "
                    + "nếu dùng token live, kiểm tra lại token/shop_id và restart Tomcat.";
        }

        if (containsIgnoreCase(message, "Shop ID is invalid")
                || containsIgnoreCase(codeMessage, "SHOP_NOT_FOUND")) {
            return "GHN shop_id không hợp lệ. Vui lòng kiểm tra lại shop_id trong cấu hình.";
        }

        if (!isBlank(codeMessageValue)) {
            return "GHN error: " + codeMessageValue;
        }

        if (!isBlank(message)) {
            return "GHN error: " + message;
        }

        if (rawBody != null && !rawBody.isBlank()) {
            return "GHN error: " + rawBody;
        }

        return "GHN trả về lỗi không xác định.";
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

    private Integer extractInt(JsonObject object, String key) {
        if (object == null || object.get(key) == null || object.get(key).isJsonNull()) {
            return null;
        }
        try {
            return object.get(key).getAsInt();
        } catch (Exception ignored) {
            return null;
        }
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

    private String resolveProvinceName(Address address) {
        if (address == null) {
            return null;
        }
        String direct = firstNonBlank(address.getEffectiveGhnProvinceName(), null);
        if (!isBlank(direct)) {
            return direct;
        }
        direct = firstNonBlank(address.getProvince(), address.getCity());
        if (!isBlank(direct)) {
            return direct;
        }
        if (address.getProvinceEntity() != null) {
            return firstNonBlank(address.getProvinceEntity().getGhnProvinceName(), null);
        }
        return null;
    }

    private String resolveDistrictName(Address address) {
        if (address == null) {
            return null;
        }
        String direct = firstNonBlank(address.getEffectiveGhnDistrictName(), null);
        if (!isBlank(direct)) {
            return direct;
        }
        direct = firstNonBlank(address.getDistrict(), null);
        if (!isBlank(direct)) {
            return direct;
        }
        if (address.getWardEntity() != null) {
            return firstNonBlank(address.getWardEntity().getGhnDistrictName(), null);
        }
        return null;
    }

    private String resolveWardName(Address address) {
        if (address == null) {
            return null;
        }
        String direct = firstNonBlank(address.getEffectiveGhnWardName(), null);
        if (!isBlank(direct)) {
            return direct;
        }
        direct = firstNonBlank(address.getWard(), null);
        if (!isBlank(direct)) {
            return direct;
        }
        if (address.getWardEntity() != null) {
            return firstNonBlank(address.getWardEntity().getGhnWardName(), null);
        }
        return null;
    }

    private Integer resolveGhnDistrictId(Address address) {
        if (address == null) {
            return null;
        }
        if (address.getGhnDistrictId() != null) {
            return address.getGhnDistrictId();
        }
        return address.getWardEntity() == null ? null : address.getWardEntity().getGhnDistrictId();
    }

    private String resolveGhnWardCode(Address address) {
        if (address == null) {
            return null;
        }
        if (address.getGhnWardCode() != null && !address.getGhnWardCode().isBlank()) {
            return address.getGhnWardCode();
        }
        return address.getWardEntity() == null ? null : address.getWardEntity().getGhnWardCode();
    }

    private String resolveRecipientPhone(Order order) throws IOException {
        String receiverPhone = normalizePhone(order.getAddress() == null ? null : order.getAddress().getReceiverPhone());
        if (isValidGhnPhone(receiverPhone)) {
            return receiverPhone;
        }

        String userPhone = normalizePhone(order.getUser() == null ? null : order.getUser().getPhone());
        if (isValidGhnPhone(userPhone)) {
            return userPhone;
        }

        throw new IOException("Số điện thoại người nhận không hợp lệ. Vui lòng cập nhật lại địa chỉ giao hàng.");
    }

    private String buildRecipientAddress(Address address, String wardName, String districtName, String provinceName) {
        String detail = firstNonBlank(address.getAddressDetail(), "");
        StringBuilder builder = new StringBuilder(detail);
        appendAddressPart(builder, wardName);
        appendAddressPart(builder, districtName);
        appendAddressPart(builder, provinceName);
        return builder.toString();
    }

    private void appendAddressPart(StringBuilder builder, String part) {
        if (isBlank(part)) {
            return;
        }
        if (!builder.isEmpty()) {
            builder.append(", ");
        }
        builder.append(part.trim());
    }

    private String normalizePhone(String phone) {
        if (phone == null) {
            return null;
        }
        String normalized = phone.trim().replaceAll("[\\s\\-().]", "");
        if (normalized.startsWith("+84")) {
            normalized = "0" + normalized.substring(3);
        } else if (normalized.startsWith("84") && normalized.length() > 2) {
            normalized = "0" + normalized.substring(2);
        }
        return normalized;
    }

    private boolean isValidGhnPhone(String phone) {
        return phone != null && phone.matches("^0(3|5|7|8|9)\\d{8}$");
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

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private boolean containsIgnoreCase(String source, String needle) {
        return source != null && needle != null && source.toLowerCase(Locale.ROOT).contains(needle.toLowerCase(Locale.ROOT));
    }

    private String resolveBaseUrl() {
        String rawBaseUrl;
        if ("live".equals(mode)) {
            rawBaseUrl = property("ghn.base_url", "https://online-gateway.ghn.vn/shiip/public-api");
        } else {
            rawBaseUrl = property("ghn.sandbox_base_url", "https://dev-online-gateway.ghn.vn/shiip/public-api");
        }
        return normalizeBaseUrl(rawBaseUrl);
    }

    private String normalizedMode(String value) {
        return value == null ? "simulation" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeBaseUrl(String value) {
        if (value == null || value.isBlank()) {
            return "https://online-gateway.ghn.vn/shiip/public-api";
        }

        String normalized = value.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        if (normalized.endsWith("/v2")) {
            normalized = normalized.substring(0, normalized.length() - 3);
        }
        return normalized;
    }

    private record AvailableService(int serviceId, int serviceTypeId) {
    }


}
