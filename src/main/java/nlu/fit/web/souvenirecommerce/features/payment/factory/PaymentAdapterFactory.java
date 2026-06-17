package nlu.fit.web.souvenirecommerce.features.payment.factory;

import nlu.fit.web.souvenirecommerce.features.payment.adapter.VnPayAdapter;
import nlu.fit.web.souvenirecommerce.features.payment.port.PaymentProviderAdapter;
import nlu.fit.web.souvenirecommerce.model.enums.PaymentProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PaymentAdapterFactory {
    
    private static final Map<PaymentProvider, PaymentProviderAdapter> adapters = new ConcurrentHashMap<>();

    static {
        adapters.put(PaymentProvider.VNPAY, new VnPayAdapter());
        // Can register other adapters like MOMO, STRIPE here in the future
    }

    public static PaymentProviderAdapter getAdapter(PaymentProvider provider) {
        PaymentProviderAdapter adapter = adapters.get(provider);
        if (adapter == null) {
            throw new IllegalArgumentException("No PaymentProviderAdapter found for provider: " + provider);
        }
        return adapter;
    }
}
