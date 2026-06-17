package nlu.fit.web.souvenirecommerce.features.payment.event;

import lombok.Getter;

@Getter
public class PaymentSucceededEvent extends PaymentEvent {
    private final String providerTransactionRef;

    public PaymentSucceededEvent(Object source, Long orderId, Long paymentTransactionId, String providerTransactionRef) {
        super(source, orderId, paymentTransactionId);
        this.providerTransactionRef = providerTransactionRef;
    }
}
