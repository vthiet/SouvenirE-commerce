package nlu.fit.web.souvenirecommerce.features.payment.event;

import lombok.Getter;

@Getter
public abstract class PaymentEvent {
    private final Object source;
    private final Long orderId;
    private final Long paymentTransactionId;
    private final long timestamp;

    public PaymentEvent(Object source, Long orderId, Long paymentTransactionId) {
        this.source = source;
        this.orderId = orderId;
        this.paymentTransactionId = paymentTransactionId;
        this.timestamp = System.currentTimeMillis();
    }
}
