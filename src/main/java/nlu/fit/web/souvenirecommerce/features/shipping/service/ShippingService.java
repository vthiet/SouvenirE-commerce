package nlu.fit.web.souvenirecommerce.features.shipping.service;

import nlu.fit.web.souvenirecommerce.features.shipping.ShippingProvider;

public class ShippingService {
    private final ShippingProvider defaultProvider = new GhnService();

    public ShippingProvider getActiveProvider() {
        // This abstracts the provider so we can easily swap implementations in the future.
        return defaultProvider;
    }
}
