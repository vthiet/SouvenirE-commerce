package nlu.fit.web.souvenirecommerce.features.payment.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentContext {
    private final String clientIp;
    private final String returnUrl;
}
