package nlu.fit.web.souvenirecommerce.features.shipping.service;

import nlu.fit.web.souvenirecommerce.common.utils.GhnAddressSeeder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public final class GhnMappingSyncJob {
    private static final Logger log = LoggerFactory.getLogger(GhnMappingSyncJob.class);
    private static final AtomicBoolean running = new AtomicBoolean(false);
    private static final AtomicReference<String> lastStatus = new AtomicReference<>("Chưa đồng bộ GHN mapping.");
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private GhnMappingSyncJob() {
    }

    public static boolean start() {
        if (!running.compareAndSet(false, true)) {
            return false;
        }

        lastStatus.set("GHN mapping đang được đồng bộ trong nền...");

        Thread worker = new Thread(() -> {
            try {
                new GhnAddressSeeder().seed();
                lastStatus.set("Đồng bộ GHN mapping thành công lúc " + LocalDateTime.now().format(formatter) + ".");
                log.info("GHN mapping sync completed successfully.");
            } catch (Exception e) {
                lastStatus.set("Đồng bộ GHN mapping thất bại: " + e.getMessage());
                log.error("GHN mapping sync failed", e);
            } finally {
                running.set(false);
            }
        }, "ghn-mapping-sync");
        worker.setDaemon(true);
        worker.setUncaughtExceptionHandler((thread, error) -> {
            lastStatus.set("Đồng bộ GHN mapping thất bại: " + error.getMessage());
            log.error("Unhandled error in {}", thread.getName(), error);
            running.set(false);
        });
        worker.start();

        return true;
    }

    public static boolean isRunning() {
        return running.get();
    }

    public static String getLastStatus() {
        return lastStatus.get();
    }
}
