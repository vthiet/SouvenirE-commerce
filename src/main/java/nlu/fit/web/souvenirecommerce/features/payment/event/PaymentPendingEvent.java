package nlu.fit.web.souvenirecommerce.features.payment.event;

public class PaymentPendingEvent extends PaymentEvent {
    public PaymentPendingEvent(Object source, Long orderId, Long paymentTransactionId) {
        super(source, orderId, paymentTransactionId);
    }
}
