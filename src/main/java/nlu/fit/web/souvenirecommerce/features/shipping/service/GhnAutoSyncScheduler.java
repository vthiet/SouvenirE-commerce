package nlu.fit.web.souvenirecommerce.features.shipping.service;

/**
 * @deprecated Replaced by {@link ShippingAutoSyncScheduler}.
 * This class is kept as a thin shell so that any external references do not
 * break at compile time.  All logic has been moved to the provider-agnostic
 * {@link ShippingAutoSyncScheduler}.
 */
@Deprecated
public final class GhnAutoSyncScheduler {

    private GhnAutoSyncScheduler() {
    }

    /** @deprecated Use {@link ShippingAutoSyncScheduler#start()} instead. */
    @Deprecated
    public static void start() {
        ShippingAutoSyncScheduler.start();
    }

    /** @deprecated Use {@link ShippingAutoSyncScheduler#shutdown()} instead. */
    @Deprecated
    public static void shutdown() {
        ShippingAutoSyncScheduler.shutdown();
    }
}
