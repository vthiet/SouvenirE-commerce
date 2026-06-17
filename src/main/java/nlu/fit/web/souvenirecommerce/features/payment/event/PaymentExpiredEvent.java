package nlu.fit.web.souvenirecommerce.features.payment.event;

public class PaymentExpiredEvent extends PaymentEvent {
    public PaymentExpiredEvent(Object source, Long orderId, Long paymentTransactionId) {
        super(source, orderId, paymentTransactionId);
    }
}
