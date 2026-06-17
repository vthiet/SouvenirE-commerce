package nlu.fit.web.souvenirecommerce.features.payment.gateway;

import nlu.fit.web.souvenirecommerce.model.enums.PaymentMethod;
import nlu.fit.web.souvenirecommerce.features.payment.dto.PaymentContext;
import nlu.fit.web.souvenirecommerce.features.payment.dto.PaymentPreparation;
import nlu.fit.web.souvenirecommerce.model.entity.Order;

public interface PaymentGateway {
    PaymentMethod method();

    default boolean isAvailable() {
        return true;
    }

    PaymentPreparation prepare(Order order, PaymentContext context);
}
