package nlu.fit.web.souvenirecommerce.features.order.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Setter
@Getter
public class OrderListDTO {
    private int orderId;
    private Date orderDate;
    private String statusText;
    private String paymentStatus;
    private double totalAmount;
    private String repayUrl;
    private final List<OrderItemDTO> items = new ArrayList<>();

    public String getStatusText() {
        if (statusText == null || statusText.isBlank()) {
            return "Đang xử lý";
        }
        return switch (statusText) {
            case "PENDING_PAYMENT" -> "Chờ thanh toán";
            case "WAIT_CONFIRM", "PENDING" -> "Chờ xác nhận";
            case "CONFIRMED" -> "Đã xác nhận";
            case "SHIPPED" -> "Đang giao";
            case "DELIVERED" -> "Đã giao";
            case "COMPLETED" -> "Hoàn thành";
            case "CANCELLED" -> "Đã hủy";
            case "PAYMENT_FAILED" -> "Thanh toán thất bại";
            default -> statusText;
        };
    }

    public int getItemCount() {
        return items.stream().mapToInt(OrderItemDTO::getQuantity).sum();
    }

}
