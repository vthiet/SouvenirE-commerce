package nlu.fit.web.souvenirecommerce.features.payment.event;

import lombok.Getter;
import nlu.fit.web.souvenirecommerce.model.enums.PaymentMethod;

import java.math.BigDecimal;

@Getter
public class PaymentCreatedEvent extends PaymentEvent {
    private final BigDecimal amount;
    private final PaymentMethod method;

    public PaymentCreatedEvent(Object source, Long orderId, Long paymentTransactionId, BigDecimal amount, PaymentMethod method) {
        super(source, orderId, paymentTransactionId);
        this.amount = amount;
        this.method = method;
    }
}
