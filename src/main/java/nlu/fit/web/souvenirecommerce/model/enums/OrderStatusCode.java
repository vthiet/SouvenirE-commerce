package nlu.fit.web.souvenirecommerce.model.enums;

import lombok.Getter;

@Getter
public enum OrderStatusCode {
    PENDING("Đang xử lý"),
    PENDING_PAYMENT("Chờ thanh toán"),
    PAYMENT_FAILED("Thanh toán thất bại"),
    PAID("Đã thanh toán"),
    CANCELLED("Đã hủy"),
    COMPLETED("Hoàn thành");

    private final String description;

    OrderStatusCode(String description) {
        this.description = description;
    }
}
