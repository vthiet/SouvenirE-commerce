package nlu.fit.web.souvenirecommerce.features.order.payment;

import nlu.fit.web.souvenirecommerce.model.enums.PaymentMethod;
import nlu.fit.web.souvenirecommerce.features.order.dto.PaymentContext;
import nlu.fit.web.souvenirecommerce.features.order.dto.PaymentPreparation;
import nlu.fit.web.souvenirecommerce.model.entity.Order;

public interface PaymentGateway {
    PaymentMethod method();

    default boolean isAvailable() {
        return true;
    }

    PaymentPreparation prepare(Order order, PaymentContext context);
}
