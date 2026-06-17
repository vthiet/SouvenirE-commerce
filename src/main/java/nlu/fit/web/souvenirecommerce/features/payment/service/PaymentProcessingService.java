package nlu.fit.web.souvenirecommerce.features.payment.service;

import nlu.fit.web.souvenirecommerce.common.event.EventBus;
import nlu.fit.web.souvenirecommerce.core.logging.AuditLogService;
import nlu.fit.web.souvenirecommerce.features.order.dto.CheckoutException;
import nlu.fit.web.souvenirecommerce.features.payment.repository.PaymentTransactionRepository;
import nlu.fit.web.souvenirecommerce.features.payment.model.PaymentCallbackResult;
import nlu.fit.web.souvenirecommerce.features.payment.event.PaymentCreatedEvent;
import nlu.fit.web.souvenirecommerce.features.payment.event.PaymentFailedEvent;
import nlu.fit.web.souvenirecommerce.features.payment.event.PaymentPendingEvent;
import nlu.fit.web.souvenirecommerce.features.payment.event.PaymentSucceededEvent;
import nlu.fit.web.souvenirecommerce.features.payment.factory.PaymentAdapterFactory;
import nlu.fit.web.souvenirecommerce.features.payment.port.PaymentProviderAdapter;
import nlu.fit.web.souvenirecommerce.model.entity.PaymentTransaction;
import nlu.fit.web.souvenirecommerce.model.enums.PaymentMethod;
import nlu.fit.web.souvenirecommerce.model.enums.PaymentProvider;
import nlu.fit.web.souvenirecommerce.model.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public class PaymentProcessingService {
    private final PaymentTransactionRepository paymentRepository = new PaymentTransactionRepository();

    public PaymentTransaction createPayment(Long orderId, BigDecimal amount, PaymentMethod method, PaymentProvider provider) {
        PaymentTransaction transaction = PaymentTransaction.builder()
                .orderId(orderId)
                .amount(amount)
                .method(method)
                .provider(provider)
                .status(PaymentStatus.CREATED)
                .build();
        
        transaction = paymentRepository.save(transaction)
                .orElseThrow(() -> new CheckoutException("Không thể tạo giao dịch thanh toán."));

        EventBus.publish(new PaymentCreatedEvent(this, orderId, transaction.getId(), amount, method));
        return transaction;
    }

    public String generatePaymentUrl(Long transactionId, String clientIp, String returnUrl) {
        PaymentTransaction transaction = paymentRepository.findById(transactionId)
                .orElseThrow(() -> new CheckoutException("Không tìm thấy giao dịch thanh toán."));

        if (transaction.getStatus() == PaymentStatus.SUCCESS || transaction.getStatus() == PaymentStatus.SETTLED) {
            throw new CheckoutException("Giao dịch đã được thanh toán.");
        }

        PaymentProviderAdapter adapter = PaymentAdapterFactory.getAdapter(transaction.getProvider());
        String url = adapter.createPaymentUrl(transaction.getId(), transaction.getOrderId(), transaction.getAmount(), clientIp, returnUrl);

        transaction.setStatus(PaymentStatus.PENDING);
        transaction.setPaymentUrl(url);
        paymentRepository.update(transaction);

        EventBus.publish(new PaymentPendingEvent(this, transaction.getOrderId(), transaction.getId()));

        return url;
    }

    public String createRetryUrl(Long orderId, Long userId, String clientIp, String returnUrl) {
        nlu.fit.web.souvenirecommerce.model.entity.Order order = new nlu.fit.web.souvenirecommerce.features.order.repository.OrderRepository().findById(orderId)
                .orElseThrow(() -> new CheckoutException("Không tìm thấy đơn hàng."));
        
        if (!order.getUser().getId().equals(userId)) {
            throw new CheckoutException("Không có quyền thực hiện.");
        }

        PaymentTransaction newTransaction = createPayment(orderId, order.getTotalAmount(), PaymentMethod.VNPAY_QR, PaymentProvider.VNPAY);
        return generatePaymentUrl(newTransaction.getId(), clientIp, returnUrl);
    }

    public PaymentCallbackResult processWebhook(PaymentProvider provider, Map<String, String> params) {
        PaymentProviderAdapter adapter = PaymentAdapterFactory.getAdapter(provider);

        if (!adapter.verifySignature(params)) {
            return result(PaymentCallbackResult.Outcome.INVALID_REQUEST, false, null);
        }

        Long transactionId = adapter.getTransactionId(params);
        if (transactionId == null) {
            return result(PaymentCallbackResult.Outcome.INVALID_REQUEST, false, null);
        }

        PaymentTransaction transaction = paymentRepository.findById(transactionId).orElse(null);
        if (transaction == null) {
            return result(PaymentCallbackResult.Outcome.ORDER_NOT_FOUND, false, null);
        }

        BigDecimal callbackAmount = adapter.getAmount(params);
        if (callbackAmount == null || transaction.getAmount().compareTo(callbackAmount) != 0) {
            return result(PaymentCallbackResult.Outcome.INVALID_AMOUNT, false, transaction);
        }

        if (transaction.getStatus() == PaymentStatus.SUCCESS || transaction.getStatus() == PaymentStatus.SETTLED) {
            // Idempotency: Ignore if already processed successfully
            return result(PaymentCallbackResult.Outcome.ALREADY_PROCESSED, true, transaction);
        }

        boolean isSuccess = adapter.isPaymentSuccess(params);
        String responseCode = adapter.getResponseCode(params);
        String providerRef = adapter.getProviderTransactionRef(params);
        
        transaction.setResponseCode(responseCode);
        transaction.setBankCode(adapter.getBankCode(params));
        transaction.setProviderTransactionRef(providerRef);
        
        if (isSuccess) {
            transaction.setStatus(PaymentStatus.SUCCESS);
            transaction.setPaidAt(LocalDateTime.now());
            paymentRepository.update(transaction);

            EventBus.publish(new PaymentSucceededEvent(this, transaction.getOrderId(), transaction.getId(), providerRef));
            
            AuditLogService.success(PaymentProcessingService.class, (nlu.fit.web.souvenirecommerce.model.entity.User) null, "PAYMENT", "PAYMENT_SUCCESS", "PAYMENT", 
                AuditLogService.describe("transactionId", transaction.getId(), "providerRef", providerRef));
        } else {
            transaction.setStatus(PaymentStatus.FAILED);
            paymentRepository.update(transaction);

            EventBus.publish(new PaymentFailedEvent(this, transaction.getOrderId(), transaction.getId(), "Provider responded with fail code: " + responseCode));
            
            AuditLogService.failure(PaymentProcessingService.class, (nlu.fit.web.souvenirecommerce.model.entity.User) null, "PAYMENT", "PAYMENT_FAILED", "PAYMENT", 
                AuditLogService.describe("transactionId", transaction.getId(), "responseCode", responseCode));
        }

        return result(PaymentCallbackResult.Outcome.PROCESSED, isSuccess, transaction);
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
}
