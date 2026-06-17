package nlu.fit.web.souvenirecommerce.features.shipping;

import nlu.fit.web.souvenirecommerce.common.utils.ApplicationLoader;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Central registry for all registered {@link ShippingProvider} implementations.
 *
 * <p>Providers are registered at application startup (in
 * {@code DbContextListener}) and looked up by code throughout the system.
 * Adding a new carrier requires only:
 * <ol>
 *   <li>Implement {@link ShippingProvider}</li>
 *   <li>Call {@code ShippingProviderRegistry.register(new MyProvider())} during startup</li>
 * </ol>
 */
public final class ShippingProviderRegistry {

    private static final Map<String, ShippingProvider> PROVIDERS = new LinkedHashMap<>();

    private ShippingProviderRegistry() {
    }

    /**
     * Registers a provider. If a provider with the same code is already registered
     * it will be replaced (allows hot-swap in tests).
     */
    public static synchronized void register(ShippingProvider provider) {
        if (provider == null || provider.getCode() == null || provider.getCode().isBlank()) {
            throw new IllegalArgumentException("Provider and its code must not be null/blank");
        }
        PROVIDERS.put(provider.getCode().toUpperCase(), provider);
    }

    /**
     * Returns the default (active) provider as configured by
     * {@code shipping.default_provider} in {@code application.properties}.
     * Falls back to the first registered provider if the config key is absent.
     *
     * @throws IllegalStateException if no provider is registered
     */
    public static ShippingProvider getDefault() {
        String configured = ApplicationLoader.get("shipping.default_provider");
        if (configured != null && !configured.isBlank()) {
            ShippingProvider found = PROVIDERS.get(configured.trim().toUpperCase());
            if (found != null) {
                return found;
            }
        }
        // Fallback: first registered
        return PROVIDERS.values().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No shipping provider has been registered. " +
                        "Call ShippingProviderRegistry.register() during application startup."));
    }

    /**
     * Looks up a provider by its carrier code (case-insensitive).
     *
     * @throws IllegalArgumentException if not found
     */
    public static ShippingProvider getByCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Carrier code must not be blank");
        }
        ShippingProvider provider = PROVIDERS.get(code.trim().toUpperCase());
        if (provider == null) {
            throw new IllegalArgumentException("No shipping provider registered for code: " + code);
        }
        return provider;
    }

    /** Returns an unmodifiable view of all registered providers. */
    public static Collection<ShippingProvider> all() {
        return Collections.unmodifiableCollection(PROVIDERS.values());
    }

    /** Visible for testing — clears all registered providers. */
    static synchronized void clear() {
        PROVIDERS.clear();
    }
}
