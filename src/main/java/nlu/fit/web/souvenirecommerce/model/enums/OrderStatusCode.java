package nlu.fit.web.souvenirecommerce.model.enums;

import lombok.Getter;

@Getter
public enum OrderStatusCode {
    PENDING_PAYMENT("Chờ thanh toán"),
    WAIT_CONFIRM("Chờ xác nhận"),
    PENDING("Đang xử lý"),
    SHIPPING("Đang giao"),
    DELIVERED("Đã giao"),
    COMPLETED("Hoàn thành"),
    CANCELLED("Đã hủy"),
    RETURNED("Đã trả hàng"),
    PAYMENT_FAILED("Thanh toán thất bại"),
    PAID("Đã thanh toán");

    private final String description;

    OrderStatusCode(String description) {
        this.description = description;
    }

    public static OrderStatusCode fromDescription(String description) {
        if (description == null) return null;
        for (OrderStatusCode code : OrderStatusCode.values()) {
            if (code.getDescription().equalsIgnoreCase(description.trim())) {
                return code;
            }
        }
        return null;
    }
}
