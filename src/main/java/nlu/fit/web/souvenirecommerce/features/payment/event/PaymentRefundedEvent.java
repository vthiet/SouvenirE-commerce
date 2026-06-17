package nlu.fit.web.souvenirecommerce.features.payment.event;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class PaymentRefundedEvent extends PaymentEvent {
    private final BigDecimal refundAmount;

    public PaymentRefundedEvent(Object source, Long orderId, Long paymentTransactionId, BigDecimal refundAmount) {
        super(source, orderId, paymentTransactionId);
        this.refundAmount = refundAmount;
    }
}
