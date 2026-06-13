package nlu.fit.web.souvenirecommerce.features.order.payment;

import nlu.fit.web.souvenirecommerce.model.enums.PaymentMethod;
import nlu.fit.web.souvenirecommerce.features.order.dto.CheckoutException;

import java.util.EnumMap;
import java.util.Map;

public class PaymentGatewayRegistry {
    private final Map<PaymentMethod, PaymentGateway> gateways = new EnumMap<>(PaymentMethod.class);

    public PaymentGatewayRegistry() {
        register(new CodPaymentGateway());
        register(new VnPayPaymentGateway());
    }

    public PaymentGateway get(PaymentMethod method) {
        PaymentGateway gateway = gateways.get(method);
        if (gateway == null) {
            throw new CheckoutException("Phương thức thanh toán này chưa được hỗ trợ");
        }
        return gateway;
    }

    public boolean isAvailable(PaymentMethod method) {
        PaymentGateway gateway = gateways.get(method);
        return gateway != null && gateway.isAvailable();
    }

    private void register(PaymentGateway gateway) {
        gateways.put(gateway.method(), gateway);
    }
}
