package nlu.fit.web.souvenirecommerce.features.order.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentContext {
    private final String clientIp;
    private final String returnUrl;
}
