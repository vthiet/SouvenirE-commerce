package nlu.fit.web.souvenirecommerce.features.shipping.service;

import nlu.fit.web.souvenirecommerce.common.utils.ApplicationLoader;
import nlu.fit.web.souvenirecommerce.common.utils.HibernateUtil;
import nlu.fit.web.souvenirecommerce.features.order.repository.ShippingOrderRepository;
import nlu.fit.web.souvenirecommerce.features.order.service.OrderService;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Background scheduler that periodically synchronises the carrier shipment status
 * for all orders that are actively in transit.
 *
 * <p>Provider-agnostic: delegates to the currently active
 * {@link nlu.fit.web.souvenirecommerce.features.shipping.ShippingProvider} via
 * {@link OrderService#syncCarrierStatus}.
 *
 * <p>Configuration keys (read from {@code application.properties}):
 * <ul>
 *   <li>{@code shipping.auto_sync_enabled} — {@code true/false} (default {@code true})</li>
 *   <li>{@code shipping.auto_sync_initial_delay_seconds} — seconds before first run (default {@code 30})</li>
 *   <li>{@code shipping.auto_sync_interval_seconds} — seconds between runs (default {@code 300})</li>
 *   <li>{@code shipping.auto_sync_batch_size} — max orders per run (default {@code 50})</li>
 * </ul>
 */
public final class ShippingAutoSyncScheduler {

    private static final Logger log = LoggerFactory.getLogger(ShippingAutoSyncScheduler.class);
    private static final Object LOCK = new Object();
    private static final ShippingOrderRepository shippingOrderRepository = new ShippingOrderRepository();
    private static final OrderService orderService = new OrderService();

    private static ScheduledExecutorService executor;

    private ShippingAutoSyncScheduler() {
    }

    public static void start() {
        if (!booleanProperty("shipping.auto_sync_enabled", "ghn.auto_sync_enabled", true)) {
            log.info("Shipping auto-sync scheduler is disabled by configuration.");
            return;
        }

        synchronized (LOCK) {
            if (executor != null && !executor.isShutdown()) {
                log.debug("Shipping auto-sync scheduler already running.");
                return;
            }

            int initialDelaySeconds = Math.max(0,
                    intProperty("shipping.auto_sync_initial_delay_seconds", "ghn.auto_sync_initial_delay_seconds", 30));
            int intervalSeconds = Math.max(30,
                    intProperty("shipping.auto_sync_interval_seconds", "ghn.auto_sync_interval_seconds", 300));
            int batchSize = Math.max(1,
                    intProperty("shipping.auto_sync_batch_size", "ghn.auto_sync_batch_size", 50));

            ThreadFactory threadFactory = runnable -> {
                Thread thread = new Thread(runnable, "shipping-auto-sync");
                thread.setDaemon(true);
                thread.setUncaughtExceptionHandler((t, error) ->
                        log.error("Unhandled error in shipping auto-sync thread {}", t.getName(), error));
                return thread;
            };

            executor = Executors.newSingleThreadScheduledExecutor(threadFactory);
            executor.scheduleWithFixedDelay(
                    () -> runSafely(batchSize),
                    initialDelaySeconds,
                    intervalSeconds,
                    TimeUnit.SECONDS
            );

            log.info(
                    "Shipping auto-sync scheduler started. interval={}s, initialDelay={}s, batchSize={}",
                    intervalSeconds, initialDelaySeconds, batchSize
            );
        }
    }

    public static void shutdown() {
        synchronized (LOCK) {
            if (executor == null) {
                return;
            }
            log.info("Stopping shipping auto-sync scheduler.");
            executor.shutdownNow();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    log.warn("Shipping auto-sync scheduler did not stop cleanly within timeout.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                executor = null;
            }
        }
    }

    private static void runSafely(int batchSize) {
        try {
            runOnce(batchSize);
        } catch (Exception e) {
            log.error("Shipping auto-sync run failed", e);
        }
    }

    private static void runOnce(int batchSize) {
        List<Long> candidateOrderIds = inTransaction(
                () -> shippingOrderRepository.findCarrierSyncCandidateOrderIds(batchSize));

        if (candidateOrderIds.isEmpty()) {
            log.debug("Shipping auto-sync found no candidates.");
            return;
        }

        int successCount = 0;
        int failureCount = 0;
        for (Long orderId : candidateOrderIds) {
            try {
                inTransaction(() -> {
                    orderService.syncCarrierStatus(orderId, "SHIPPING_AUTO_SYNC");
                    return null;
                });
                successCount++;
            } catch (Exception e) {
                failureCount++;
                log.warn("Shipping auto-sync failed for orderId={}: {}", orderId, e.getMessage());
            }
        }

        log.info(
                "Shipping auto-sync finished. candidates={}, success={}, failed={}",
                candidateOrderIds.size(), successCount, failureCount
        );
    }

    private static <T> T inTransaction(TransactionAction<T> action) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction transaction = session.beginTransaction();
        try {
            T result = action.execute();
            transaction.commit();
            return result;
        } catch (RuntimeException | Error e) {
            rollbackQuietly(transaction);
            throw e;
        }
    }

    private static void rollbackQuietly(Transaction transaction) {
        if (transaction != null && transaction.isActive()) {
            try {
                transaction.rollback();
            } catch (Exception ignored) {
                log.debug("Ignoring rollback failure in shipping auto-sync transaction");
            }
        }
    }

    /** Reads a boolean property, falling back to a legacy key, then to the hardcoded default. */
    private static boolean booleanProperty(String key, String legacyKey, boolean fallback) {
        String value = ApplicationLoader.get(key);
        if (value == null || value.isBlank()) {
            value = ApplicationLoader.get(legacyKey);
        }
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "true", "1", "yes", "y", "on" -> true;
            case "false", "0", "no", "n", "off" -> false;
            default -> fallback;
        };
    }

    /** Reads an int property, falling back to a legacy key, then to the hardcoded default. */
    private static int intProperty(String key, String legacyKey, int fallback) {
        String value = ApplicationLoader.get(key);
        if (value == null || value.isBlank()) {
            value = ApplicationLoader.get(legacyKey);
        }
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    @FunctionalInterface
    private interface TransactionAction<T> {
        T execute();
    }
}
