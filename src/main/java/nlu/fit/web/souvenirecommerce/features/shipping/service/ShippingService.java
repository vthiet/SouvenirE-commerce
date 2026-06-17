package nlu.fit.web.souvenirecommerce.features.shipping.service;

import nlu.fit.web.souvenirecommerce.features.shipping.ShippingProvider;
import nlu.fit.web.souvenirecommerce.features.shipping.ShippingProviderRegistry;

/**
 * Thin façade over {@link ShippingProviderRegistry}.
 * Consumers that already depend on this class continue to work without change.
 */
public class ShippingService {

    /** Returns the currently active (default) shipping provider. */
    public ShippingProvider getActiveProvider() {
        return ShippingProviderRegistry.getDefault();
    }
}
