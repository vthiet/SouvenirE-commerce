package nlu.fit.web.souvenirecommerce.features.payment;

import nlu.fit.web.souvenirecommerce.features.order.dto.CheckoutException;
import nlu.fit.web.souvenirecommerce.features.order.repository.OrderStatusRepository;
import nlu.fit.web.souvenirecommerce.features.order.repository.PaymentTransactionRepository;
import nlu.fit.web.souvenirecommerce.model.entity.OrderStatus;
import nlu.fit.web.souvenirecommerce.model.entity.PaymentTransaction;
import nlu.fit.web.souvenirecommerce.model.enums.OrderStatusCode;
import nlu.fit.web.souvenirecommerce.model.enums.PaymentMethod;
import nlu.fit.web.souvenirecommerce.model.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public class PaymentProcessingService {
    private final PaymentTransactionRepository paymentRepository = new PaymentTransactionRepository();
    private final OrderStatusRepository orderStatusRepository = new OrderStatusRepository();
    private final VnPayService vnPayService = new VnPayService();

    public PaymentCallbackResult processVnPayCallback(Map<String, String> fields) {
        Long orderId = parseLong(fields.get("vnp_TxnRef"));
        Long amountRaw = parseLong(fields.get("vnp_Amount"));
        if (orderId == null || amountRaw == null || amountRaw <= 0) {
            return result(PaymentCallbackResult.Outcome.INVALID_REQUEST, false, null);
        }

        PaymentTransaction transaction = paymentRepository.findByOrderId(orderId).orElse(null);
        if (transaction == null || transaction.getMethod() != PaymentMethod.VNPAY_QR) {
            return result(PaymentCallbackResult.Outcome.ORDER_NOT_FOUND, false, null);
        }

        BigDecimal callbackAmount = BigDecimal.valueOf(amountRaw, 2);
        if (transaction.getAmount().compareTo(callbackAmount) != 0) {
            return result(PaymentCallbackResult.Outcome.INVALID_AMOUNT, false, transaction);
        }
        if (transaction.getStatus() == PaymentStatus.PAID) {
            return result(PaymentCallbackResult.Outcome.ALREADY_PROCESSED, true, transaction);
        }

        String responseCode = fields.get("vnp_ResponseCode");
        boolean successful = "00".equals(responseCode)
                && "00".equals(fields.get("vnp_TransactionStatus"));

        transaction.setResponseCode(responseCode);
        transaction.setBankCode(fields.get("vnp_BankCode"));
        transaction.setProviderTransactionRef(fields.get("vnp_TransactionNo"));
        transaction.setStatus(successful ? PaymentStatus.PAID : PaymentStatus.FAILED);
        transaction.setPaidAt(successful ? LocalDateTime.now() : null);
        transaction.getOrder().setStatus(resolveStatus(
                successful ? OrderStatusCode.PAID : OrderStatusCode.PAYMENT_FAILED));
        paymentRepository.update(transaction);

        return result(PaymentCallbackResult.Outcome.PROCESSED, successful, transaction);
    }

    public String createRetryUrl(Long orderId, Long userId, String clientIp, String returnUrl) {
        PaymentTransaction transaction = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CheckoutException("Không tìm thấy giao dịch thanh toán."));

        if (userId == null || !userId.equals(transaction.getOrder().getUser().getId())) {
            throw new CheckoutException("Bạn không có quyền thanh toán đơn hàng này.");
        }
        if (transaction.getMethod() != PaymentMethod.VNPAY_QR) {
            throw new CheckoutException("Đơn hàng này không sử dụng VNPay.");
        }
        if (transaction.getStatus() == PaymentStatus.PAID) {
            throw new CheckoutException("Đơn hàng đã được thanh toán.");
        }

        String paymentUrl = vnPayService.createPaymentUrl(
                orderId,
                transaction.getAmount().longValueExact(),
                clientIp,
                returnUrl);
        transaction.setStatus(PaymentStatus.PENDING);
        transaction.setResponseCode(null);
        transaction.setBankCode(null);
        transaction.setProviderTransactionRef(null);
        transaction.setPaidAt(null);
        transaction.setPaymentUrl(paymentUrl);
        transaction.getOrder().setStatus(resolveStatus(OrderStatusCode.PENDING_PAYMENT));
        paymentRepository.update(transaction);
        return paymentUrl;
    }

    private OrderStatus resolveStatus(OrderStatusCode code) {
        return orderStatusRepository.findByDescription(code.getDescription())
                .orElseGet(() -> orderStatusRepository.save(OrderStatus.builder()
                                .description(code.getDescription())
                                .build())
                        .orElseThrow(() -> new CheckoutException("Không thể cập nhật trạng thái đơn hàng.")));
    }

    private PaymentCallbackResult result(PaymentCallbackResult.Outcome outcome,
                                         boolean successful,
                                         PaymentTransaction transaction) {
        return PaymentCallbackResult.builder()
                .outcome(outcome)
                .successful(successful)
                .transaction(transaction)
                .build();
    }

    private Long parseLong(String value) {
        try {
            return value == null ? null : Long.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
