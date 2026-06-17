package nlu.fit.web.souvenirecommerce.features.shipping.service;

import nlu.fit.web.souvenirecommerce.common.utils.GhnAddressSeeder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Background job used by the admin settings page to synchronise GHN mapping.
 *
 * <p>Only one sync can run at a time. The latest status is stored in-memory so
 * the settings page can display progress and the result of the last run.
 */
public final class GhnMappingSyncJob {

    private static final Logger log = LoggerFactory.getLogger(GhnMappingSyncJob.class);
    private static final Object LOCK = new Object();
    private static final AtomicBoolean RUNNING = new AtomicBoolean(false);

    private static volatile LocalDateTime lastStartedAt;
    private static volatile LocalDateTime lastFinishedAt;
    private static volatile long lastDurationMillis;
    private static volatile boolean lastSuccess;
    private static volatile String lastMessage = "Chưa chạy đồng bộ GHN mapping.";
    private static volatile String lastError;
    private static volatile GhnAddressSeeder.SyncReport lastReport;

    private GhnMappingSyncJob() {
    }

    public static boolean start() {
        synchronized (LOCK) {
            if (RUNNING.get()) {
                return false;
            }

            RUNNING.set(true);
            lastStartedAt = LocalDateTime.now();
            lastFinishedAt = null;
            lastDurationMillis = 0L;
            lastSuccess = false;
            lastError = null;
            lastMessage = "Đang đồng bộ GHN mapping trong nền...";
            Thread worker = new Thread(GhnMappingSyncJob::runSafely, "ghn-mapping-sync");
            worker.setDaemon(true);
            worker.setUncaughtExceptionHandler((thread, error) ->
                    log.error("Unhandled error in GHN mapping sync thread {}", thread.getName(), error));
            worker.start();
            return true;
        }
    }

    public static SyncStatus getStatus() {
        return new SyncStatus(
                RUNNING.get(),
                lastSuccess,
                lastStartedAt,
                lastFinishedAt,
                lastDurationMillis,
                lastMessage,
                lastError,
                lastReport
        );
    }

    private static void runSafely() {
        LocalDateTime startedAt = lastStartedAt;
        try {
            GhnAddressSeeder.SyncReport report = GhnAddressSeeder.seed();
            lastReport = report;
            lastSuccess = true;
            lastError = null;
            lastMessage = buildSuccessMessage(report);
            log.info("GHN mapping sync completed: {}", lastMessage);
        } catch (Exception e) {
            lastSuccess = false;
            lastError = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            lastMessage = "GHN mapping sync failed: " + lastError;
            log.error("GHN mapping sync failed", e);
        } finally {
            lastFinishedAt = LocalDateTime.now();
            if (startedAt != null && lastFinishedAt != null) {
                lastDurationMillis = Math.max(0L, Duration.between(startedAt, lastFinishedAt).toMillis());
            }
            RUNNING.set(false);
        }
    }

    private static String buildSuccessMessage(GhnAddressSeeder.SyncReport report) {
        if (report == null) {
            return "GHN mapping đồng bộ xong.";
        }
        return "Đồng bộ xong: " + report.provincesMapped() + "/" + report.provincesTotal() + " tỉnh, "
                + report.wardsMapped() + "/" + report.wardsTotal() + " phường/xã, "
                + report.addressesBackfilled() + " địa chỉ đã cập nhật.";
    }

    public record SyncStatus(boolean running,
                             boolean lastSuccess,
                             LocalDateTime lastStartedAt,
                             LocalDateTime lastFinishedAt,
                             long lastDurationMillis,
                             String lastMessage,
                             String lastError,
                             GhnAddressSeeder.SyncReport lastReport) {
    }
}
