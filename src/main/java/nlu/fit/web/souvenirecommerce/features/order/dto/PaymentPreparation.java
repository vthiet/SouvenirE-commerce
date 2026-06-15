package nlu.fit.web.souvenirecommerce.features.order.dto;

import lombok.Builder;
import lombok.Getter;
import nlu.fit.web.souvenirecommerce.model.enums.PaymentProvider;
import nlu.fit.web.souvenirecommerce.model.enums.PaymentStatus;

@Getter
@Builder
public class PaymentPreparation {
    private final PaymentProvider provider;
    private final PaymentStatus status;
    private final String providerTransactionRef;
    private final String paymentUrl;
    private final String qrPayload;
}
