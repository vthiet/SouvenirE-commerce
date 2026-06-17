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

/**
 * GHN (Giao Hàng Nhanh) implementation of {@link ShippingProvider}.
 * All GHN-specific logic lives here; the rest of the application only knows
 * about the generic {@link ShippingProvider} interface.
 */
public class GhnService implements ShippingProvider {

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final String baseUrl = property("ghn.base_url", "https://dev-online-gateway.ghn.vn/shiip/public-api");
    private final String token = property("ghn.token", "");
    private final String shopId = property("ghn.shop_id", "");
    private final boolean simulation = !"live".equalsIgnoreCase(property("ghn.mode", "simulation"));

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

        int fromDistrictId = Integer.parseInt(property("ghn.from_district_id", "1442"));
        int serviceId = getAvailableServiceId(fromDistrictId, address.getCarrierDistrictId());

        JsonObject body = new JsonObject();
        body.addProperty("payment_type_id", 1);
        body.addProperty("required_note", "KHONGCHOXEMHANG");
        body.addProperty("service_id", serviceId);
        body.addProperty("from_name", property("ghn.from_name", "INOLA"));
        body.addProperty("from_phone", property("ghn.from_phone", "0900000000"));
        body.addProperty("from_address", property("ghn.from_address", "Dia chi cua hang"));
        body.addProperty("from_ward_name", property("ghn.from_ward_name", "Phuong Ben Nghe"));
        body.addProperty("from_district_name", property("ghn.from_district_name", "Quan 1"));
        body.addProperty("from_province_name", property("ghn.from_province_name", "Ho Chi Minh"));
        body.addProperty("to_name", address.getReceiverName());
        body.addProperty("to_phone", address.getReceiverPhone());
        body.addProperty("to_address", address.getAddressDetail());
        body.addProperty("to_ward_code", address.getCarrierWardCode());
        body.addProperty("to_district_id", address.getCarrierDistrictId());
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
        JsonArray services = JsonParser.parseString(response).getAsJsonObject().getAsJsonArray("data");
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
}
