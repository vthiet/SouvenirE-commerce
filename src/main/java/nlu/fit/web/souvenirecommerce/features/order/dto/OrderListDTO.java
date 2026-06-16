package nlu.fit.web.souvenirecommerce.features.order.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderListDTO {
    private int orderId;
    private Date orderDate;
    private String statusText;
    private String paymentStatus;
    private double totalAmount;
    private String repayUrl;
    private final List<OrderItemDTO> items = new ArrayList<>();

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

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

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getRepayUrl() {
        return repayUrl;
    }

    public void setRepayUrl(String repayUrl) {
        this.repayUrl = repayUrl;
    }

    public List<OrderItemDTO> getItems() {
        return items;
    }

    public int getItemCount() {
        return items.stream().mapToInt(OrderItemDTO::getQuantity).sum();
    }

    public String getStatusClass() {
        String status = (statusText == null ? "" : statusText).toLowerCase(Locale.ROOT);
        String payment = (paymentStatus == null ? "" : paymentStatus).toLowerCase(Locale.ROOT);
        if (status.contains("thất bại") || status.contains("payment_failed") || payment.contains("failed")) {
            return "PAYMENT_FAILED";
        }
        if (status.contains("chờ thanh toán") || status.contains("pending_payment")) {
            return "PENDING_PAYMENT";
        }
        if (status.contains("hủy") || status.contains("cancel")) {
            return "CANCELLED";
        }
        if (status.contains("hoàn thành") || status.contains("đã giao") || status.contains("completed") || status.contains("delivered")) {
            return "DELIVERED";
        }
        if (status.contains("giao") || status.contains("ship")) {
            return "SHIPPED";
        }
        if (status.contains("xử lý") || status.contains("xác nhận") || status.contains("confirm") || status.contains("pending")) {
            return "CONFIRMED";
        }
        return "PENDING";
    }

    public boolean isRepayable() {
        if (repayUrl == null || repayUrl.isBlank()) {
            return false;
        }
        String status = (statusText == null ? "" : statusText).toLowerCase(Locale.ROOT);
        String payment = (paymentStatus == null ? "" : paymentStatus).toLowerCase(Locale.ROOT);
        return status.contains("chờ thanh toán")
                || status.contains("pending_payment")
                || status.contains("thất bại")
                || payment.contains("pending")
                || payment.contains("failed");
    }
}
