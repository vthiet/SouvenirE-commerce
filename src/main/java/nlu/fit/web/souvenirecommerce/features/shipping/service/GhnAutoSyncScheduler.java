package nlu.fit.web.souvenirecommerce.features.shipping.service;

import nlu.fit.web.souvenirecommerce.common.utils.ApplicationLoader;
import nlu.fit.web.souvenirecommerce.common.utils.HibernateUtil;
import nlu.fit.web.souvenirecommerce.features.order.repository.OrderRepository;
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

public final class GhnAutoSyncScheduler {
    private static final Logger log = LoggerFactory.getLogger(GhnAutoSyncScheduler.class);
    private static final Object LOCK = new Object();
    private static final OrderRepository orderRepository = new OrderRepository();
    private static final OrderService orderService = new OrderService();

    private static ScheduledExecutorService executor;

    private GhnAutoSyncScheduler() {
    }

    public static void start() {
        if (!booleanProperty("ghn.auto_sync_enabled", true)) {
            log.info("GHN auto sync scheduler is disabled by configuration.");
            return;
        }

        synchronized (LOCK) {
            if (executor != null && !executor.isShutdown()) {
                log.debug("GHN auto sync scheduler already running.");
                return;
            }

            int initialDelaySeconds = Math.max(0, intProperty("ghn.auto_sync_initial_delay_seconds", 30));
            int intervalSeconds = Math.max(30, intProperty("ghn.auto_sync_interval_seconds", 300));
            int batchSize = Math.max(1, intProperty("ghn.auto_sync_batch_size", 50));

            ThreadFactory threadFactory = runnable -> {
                Thread thread = new Thread(runnable, "ghn-auto-sync");
                thread.setDaemon(true);
                thread.setUncaughtExceptionHandler((t, error) ->
                        log.error("Unhandled error in GHN auto sync thread {}", t.getName(), error));
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
                    "GHN auto sync scheduler started. interval={}s, initialDelay={}s, batchSize={}",
                    intervalSeconds,
                    initialDelaySeconds,
                    batchSize
            );
        }
    }

    public static void shutdown() {
        synchronized (LOCK) {
            if (executor == null) {
                return;
            }

            log.info("Stopping GHN auto sync scheduler.");
            executor.shutdownNow();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    log.warn("GHN auto sync scheduler did not stop cleanly within timeout.");
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
            log.error("GHN auto sync run failed", e);
        }
    }

    private static void runOnce(int batchSize) {
        List<Long> candidateIds = inTransaction(() -> orderRepository.findGhnSyncCandidateIds(batchSize));
        if (candidateIds.isEmpty()) {
            log.debug("GHN auto sync found no candidates.");
            return;
        }

        int successCount = 0;
        int failureCount = 0;
        for (Long orderId : candidateIds) {
            try {
                inTransaction(() -> {
                    orderService.syncGhnStatus(orderId, "GHN_AUTO_SYNC");
                    return null;
                });
                successCount++;
            } catch (Exception e) {
                failureCount++;
                log.warn("GHN auto sync failed for orderId={}: {}", orderId, e.getMessage());
            }
        }

        log.info(
                "GHN auto sync finished. candidates={}, success={}, failed={}",
                candidateIds.size(),
                successCount,
                failureCount
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
                log.debug("Ignoring rollback failure for GHN auto sync transaction");
            }
        }
    }

    private static boolean booleanProperty(String key, boolean fallback) {
        String value = ApplicationLoader.get(key);
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "true", "1", "yes", "y", "on" -> true;
            case "false", "0", "no", "n", "off" -> false;
            default -> fallback;
        };
    }

    private static int intProperty(String key, int fallback) {
        String value = ApplicationLoader.get(key);
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
