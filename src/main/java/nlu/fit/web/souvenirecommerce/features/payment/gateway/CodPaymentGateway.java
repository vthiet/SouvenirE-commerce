package nlu.fit.web.souvenirecommerce.features.payment.gateway;

import nlu.fit.web.souvenirecommerce.model.enums.PaymentMethod;
import nlu.fit.web.souvenirecommerce.model.enums.PaymentProvider;
import nlu.fit.web.souvenirecommerce.model.enums.PaymentStatus;
import nlu.fit.web.souvenirecommerce.features.payment.dto.PaymentContext;
import nlu.fit.web.souvenirecommerce.features.payment.dto.PaymentPreparation;
import nlu.fit.web.souvenirecommerce.model.entity.Order;

public class CodPaymentGateway implements PaymentGateway {
    @Override
    public PaymentMethod method() {
        return PaymentMethod.COD;
    }

    @Override
    public PaymentPreparation prepare(Order order, PaymentContext context) {
        return PaymentPreparation.builder()
                .provider(PaymentProvider.COD)
                .status(PaymentStatus.PENDING)
                .build();
    }
}
