package nlu.fit.web.souvenirecommerce.features.notification.service;

import jakarta.mail.MessagingException;
import nlu.fit.web.souvenirecommerce.common.utils.EmailUtil;
import nlu.fit.web.souvenirecommerce.model.entity.Order;
import nlu.fit.web.souvenirecommerce.model.entity.OrderItem;
import nlu.fit.web.souvenirecommerce.model.entity.Address;
import nlu.fit.web.souvenirecommerce.model.entity.ShippingOrder;
import nlu.fit.web.souvenirecommerce.model.enums.EmailType;
import nlu.fit.web.souvenirecommerce.model.enums.OrderStatusCode;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class NotificationService {

    public void sendOrderStatusEmail(Order order, OrderStatusCode previousStatus, OrderStatusCode newStatus, String description) {
        if (order == null || order.getUser() == null) {
            return;
        }

        String recipientEmail = order.getUser().getEmail();
        if (recipientEmail == null || recipientEmail.isBlank()) {
            return;
        }

        String orderCode = order.getOrderCode();
        String subject = String.format("[INOLA] Đơn hàng #%s: %s", orderCode, newStatus.getDescription());
        String htmlContent = buildHtmlContent(order, newStatus, description);

        sendAsync(recipientEmail, subject, htmlContent);
    }

    public void sendPaymentSuccessEmail(Long orderId) {
        // Fetch order first
        CompletableFuture.runAsync(() -> {
            try {
                Order order = new nlu.fit.web.souvenirecommerce.features.order.service.OrderService().getOrderById(orderId);
                if (order != null) {
                    sendOrderStatusEmail(order, OrderStatusCode.PENDING_PAYMENT, OrderStatusCode.PAID, "Khách hàng thanh toán thành công qua VNPay.");
                }
            } catch (Exception e) {
                System.err.println("Failed to send payment success notification for order " + orderId + ": " + e.getMessage());
            }
        });
    }

    public void sendPaymentFailedEmail(Long orderId, String reason) {
        CompletableFuture.runAsync(() -> {
            try {
                Order order = new nlu.fit.web.souvenirecommerce.features.order.service.OrderService().getOrderById(orderId);
                if (order != null) {
                    sendOrderStatusEmail(order, OrderStatusCode.PENDING_PAYMENT, OrderStatusCode.PAYMENT_FAILED, "Thanh toán thất bại: " + reason);
                }
            } catch (Exception e) {
                System.err.println("Failed to send payment failed notification for order " + orderId + ": " + e.getMessage());
            }
        });
    }

    private void sendAsync(String to, String subject, String content) {
        CompletableFuture.runAsync(() -> {
            try {
                EmailUtil.send(to, subject, content, EmailType.HTML.getMimeType());
                System.out.println("Notification email successfully sent to " + to + " for subject: " + subject);
            } catch (MessagingException e) {
                System.err.println("Failed to send notification email to " + to + ": " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private String buildHtmlContent(Order order, OrderStatusCode status, String statusDescription) {
        String statusColor = getStatusColor(status);
        String formattedDate = order.getCreatedAt() != null 
                ? order.getOrderDate().toString() // Default format, can be customized
                : "";
        if (order.getOrderDate() != null) {
            try {
                formattedDate = order.getCreatedAt().toString();
            } catch (Exception ignored) {}
        }

        Address address = order.getAddress();
        String receiverName = "";
        String receiverPhone = "";
        String fullAddressStr = "";
        if (address != null) {
            receiverName = address.getReceiverName();
            receiverPhone = address.getReceiverPhone();
            fullAddressStr = address.getAddressDetail() + ", " + address.getWard() + ", " + address.getDistrict() + ", " + address.getProvince();
        } else if (order.getUser() != null) {
            receiverName = order.getUser().getFullName();
            receiverPhone = order.getUser().getPhone();
        }

        // Format Payment Method Name
        String paymentMethod = order.getPaymentMethod();
        String paymentMethodDisplayName = getPaymentMethodName(paymentMethod);

        // Format Shipping Carrier Info
        String carrierInfo = getCarrierInfo(order);

        // Generate Items Rows
        StringBuilder itemsHtml = new StringBuilder();
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                BigDecimal price = item.getPriceAtPurchase();
                int qty = item.getQuantity();
                BigDecimal subtotal = price.multiply(BigDecimal.valueOf(qty));
                itemsHtml.append(String.format(
                    "<tr>" +
                    "  <td>%s</td>" +
                    "  <td class='qty'>%d</td>" +
                    "  <td class='price'>%s</td>" +
                    "  <td class='price'>%s</td>" +
                    "</tr>",
                    item.getProductName(),
                    qty,
                    formatVnd(price),
                    formatVnd(subtotal)
                ));
            }
        }

        BigDecimal subtotalSum = order.getTotalAmount().subtract(order.getShippingFee() != null ? order.getShippingFee() : BigDecimal.ZERO);

        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "  <meta charset='utf-8'>" +
                "  <style>" +
                "    body { font-family: 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; line-height: 1.6; color: #2d3748; background-color: #f7fafc; margin: 0; padding: 0; }" +
                "    .wrapper { width: 100%; background-color: #f7fafc; padding: 30px 0; }" +
                "    .container { max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05); }" +
                "    .header { background: linear-gradient(135deg, #1a365d 0%, #2a4365 100%); color: #ffffff; padding: 30px 20px; text-align: center; }" +
                "    .header h1 { margin: 0; font-size: 24px; font-weight: 700; letter-spacing: 0.5px; }" +
                "    .header p { margin: 5px 0 0 0; font-size: 14px; opacity: 0.85; }" +
                "    .body { padding: 30px 20px; }" +
                "    .status-banner { border-left: 4px solid " + statusColor + "; background-color: #f8fafc; padding: 15px 20px; margin-bottom: 25px; border-radius: 0 4px 4px 0; }" +
                "    .status-title { font-weight: 700; font-size: 16px; color: " + statusColor + "; margin-bottom: 5px; }" +
                "    .status-desc { font-size: 14px; margin: 0; color: #4a5568; }" +
                "    .info-grid { display: table; width: 100%; margin-bottom: 25px; border-collapse: collapse; }" +
                "    .info-col { display: table-cell; width: 50%; vertical-align: top; padding-right: 10px; }" +
                "    .info-col-last { display: table-cell; width: 50%; vertical-align: top; padding-left: 10px; }" +
                "    .info-card { background-color: #f8fafc; border: 1px solid #edf2f7; border-radius: 6px; padding: 15px; min-height: 120px; }" +
                "    .info-card h3 { margin: 0 0 8px 0; font-size: 12px; text-transform: uppercase; color: #718096; letter-spacing: 0.5px; }" +
                "    .info-card p { margin: 0; font-size: 14px; color: #4a5568; line-height: 1.5; }" +
                "    .order-table { width: 100%; border-collapse: collapse; margin-bottom: 25px; }" +
                "    .order-table th { text-align: left; padding: 12px 10px; background-color: #f8fafc; font-size: 12px; text-transform: uppercase; color: #718096; border-bottom: 2px solid #edf2f7; }" +
                "    .order-table td { padding: 12px 10px; font-size: 14px; border-bottom: 1px solid #edf2f7; }" +
                "    .order-table td.qty { text-align: center; }" +
                "    .order-table td.price { text-align: right; }" +
                "    .totals-wrapper { width: 100%; margin-bottom: 25px; text-align: right; }" +
                "    .totals-table { display: inline-table; width: 280px; border-collapse: collapse; }" +
                "    .totals-table td { padding: 6px 10px; font-size: 14px; }" +
                "    .totals-table td.label { text-align: right; color: #718096; }" +
                "    .totals-table td.value { text-align: right; font-weight: 600; color: #2d3748; }" +
                "    .totals-table tr.grand-total td { border-top: 1px solid #edf2f7; padding-top: 10px; font-size: 16px; font-weight: 700; color: #1a365d; }" +
                "    .clear { clear: both; }" +
                "    .footer { background-color: #f8fafc; padding: 25px 20px; text-align: center; font-size: 12px; color: #a0aec0; border-top: 1px solid #edf2f7; }" +
                "    .footer p { margin: 5px 0; }" +
                "  </style>" +
                "</head>" +
                "<body>" +
                "  <div class='wrapper'>" +
                "    <div class='container'>" +
                "      <div class='header'>" +
                "        <h1>QUÀ LƯU NIỆM INOLA</h1>" +
                "        <p>Mã đơn hàng: " + order.getOrderCode() + "</p>" +
                "      </div>" +
                "      <div class='body'>" +
                "        <div class='status-banner'>" +
                "          <div class='status-title'>" + status.getDescription() + "</div>" +
                "          <p class='status-desc'>" + (statusDescription != null ? statusDescription : "") + "</p>" +
                "        </div>" +
                "        <div class='info-grid'>" +
                "          <div class='info-col'>" +
                "            <div class='info-card'>" +
                "              <h3>Thông tin nhận hàng</h3>" +
                "              <p>" +
                "                <strong>" + receiverName + "</strong><br/>" +
                "                SĐT: " + receiverPhone + "<br/>" +
                "                Địa chỉ: " + fullAddressStr + "" +
                "              </p>" +
                "            </div>" +
                "          </div>" +
                "          <div class='info-col-last'>" +
                "            <div class='info-card'>" +
                "              <h3>Thanh toán & Vận chuyển</h3>" +
                "              <p>" +
                "                <strong>Phương thức TT:</strong><br/>" +
                "                " + paymentMethodDisplayName + "<br/>" +
                "                <span style='margin-top: 8px; display: inline-block;'>" + carrierInfo + "</span>" +
                "              </p>" +
                "            </div>" +
                "          </div>" +
                "        </div>" +
                "        <table class='order-table'>" +
                "          <thead>" +
                "            <tr>" +
                "              <th>Sản phẩm</th>" +
                "              <th style='text-align: center;'>SL</th>" +
                "              <th style='text-align: right;'>Đơn giá</th>" +
                "              <th style='text-align: right;'>Thành tiền</th>" +
                "            </tr>" +
                "          </thead>" +
                "          <tbody>" +
                "            " + itemsHtml.toString() + "" +
                "          </tbody>" +
                "        </table>" +
                "        <div class='totals-wrapper'>" +
                "          <table class='totals-table'>" +
                "            <tr>" +
                "              <td class='label'>Tạm tính</td>" +
                "              <td class='value'>" + formatVnd(subtotalSum) + "</td>" +
                "            </tr>" +
                "            <tr>" +
                "              <td class='label'>Phí vận chuyển</td>" +
                "              <td class='value'>" + formatVnd(order.getShippingFee()) + "</td>" +
                "            </tr>" +
                "            <tr class='grand-total'>" +
                "              <td class='label'>Tổng thanh toán</td>" +
                "              <td class='value'>" + formatVnd(order.getTotalAmount()) + "</td>" +
                "            </tr>" +
                "          </table>" +
                "        </div>" +
                "        <div class='clear'></div>" +
                "      </div>" +
                "      <div class='footer'>" +
                "        <p>Cảm ơn bạn đã tin tưởng mua sắm tại INOLA!</p>" +
                "        <p>Nếu bạn có bất kỳ câu hỏi nào, vui lòng liên hệ hotline 1900 6000 hoặc phản hồi trực tiếp email này.</p>" +
                "        <p>&copy; 2026 INOLA Souvenirs. All rights reserved.</p>" +
                "      </div>" +
                "    </div>" +
                "  </div>" +
                "</body>" +
                "</html>";
    }

    private String getStatusColor(OrderStatusCode status) {
        if (status == null) return "#718096";
        return switch (status) {
            case WAIT_CONFIRM, PENDING_PAYMENT -> "#d97706"; // amber
            case PENDING -> "#2563eb"; // blue
            case SHIPPING -> "#3b82f6"; // sky blue
            case DELIVERED, COMPLETED, PAID -> "#16a34a"; // green
            case CANCELLED, PAYMENT_FAILED -> "#dc2626"; // red
            default -> "#4b5563"; // gray
        };
    }

    private String getPaymentMethodName(String method) {
        if (method == null) return "Chưa xác định";
        return switch (method) {
            case "COD" -> "Thanh toán khi nhận hàng (COD)";
            case "VNPAY_QR" -> "Thanh toán trực tuyến qua VNPay QR";
            default -> method;
        };
    }

    private String getCarrierInfo(Order order) {
        ShippingOrder activeShipment = order.getActiveShippingOrder();
        if (activeShipment != null) {
            String carrierName = activeShipment.getCarrierCode();
            if ("GHN".equalsIgnoreCase(carrierName)) {
                carrierName = "Giao Hàng Nhanh (GHN)";
            }
            String trackingCode = activeShipment.getTrackingCode();
            if (trackingCode == null || trackingCode.isBlank()) {
                trackingCode = "Đang xử lý...";
            }
            return "<b>Đơn vị vận chuyển:</b> " + carrierName + "<br/>" +
                   "<b>Mã vận đơn:</b> " + trackingCode;
        } else {
            String carrierCode = order.getPreferredCarrierCode();
            if (carrierCode != null && !carrierCode.isBlank()) {
                String carrierName = "GHN".equalsIgnoreCase(carrierCode) ? "Giao Hàng Nhanh (GHN)" : carrierCode;
                return "<b>Đơn vị vận chuyển (dự kiến):</b> " + carrierName;
            }
        }
        return "<b>Vận chuyển:</b> Chưa giao cho đơn vị vận chuyển";
    }

    private String formatVnd(BigDecimal amount) {
        if (amount == null) return "0đ";
        try {
            return NumberFormat.getIntegerInstance(new Locale("vi", "VN")).format(amount) + "đ";
        } catch (Exception e) {
            return amount.toPlainString() + "đ";
        }
    }
}
