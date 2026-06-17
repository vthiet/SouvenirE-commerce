package nlu.fit.web.souvenirecommerce.features.payment.event;

import lombok.Getter;

@Getter
public class PaymentFailedEvent extends PaymentEvent {
    private final String reason;

    public PaymentFailedEvent(Object source, Long orderId, Long paymentTransactionId, String reason) {
        super(source, orderId, paymentTransactionId);
        this.reason = reason;
    }
}
