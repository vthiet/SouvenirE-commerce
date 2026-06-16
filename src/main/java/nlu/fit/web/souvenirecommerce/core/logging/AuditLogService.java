package nlu.fit.web.souvenirecommerce.core.logging;

import nlu.fit.web.souvenirecommerce.model.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;

public final class AuditLogService {

    private static final String PREFIX = "AUDIT";

    private AuditLogService() {
    }

    public static void success(Class<?> source, User actor, String category, String action, String entity, String details) {
        record(source, resolveActorLabel(actor), category, action, entity, "SUCCESS", details, false);
    }

    public static void success(Class<?> source, String actorLabel, String category, String action, String entity, String details) {
        record(source, actorLabel, category, action, entity, "SUCCESS", details, false);
    }

    public static void failure(Class<?> source, User actor, String category, String action, String entity, String details) {
        record(source, resolveActorLabel(actor), category, action, entity, "FAILED", details, true);
    }

    public static void failure(Class<?> source, String actorLabel, String category, String action, String entity, String details) {
        record(source, actorLabel, category, action, entity, "FAILED", details, true);
    }

    public static String describe(Object... keyValues) {
        if (keyValues == null || keyValues.length == 0) {
            return "";
        }
        if (keyValues.length % 2 != 0) {
            throw new IllegalArgumentException("Key/value pairs must be provided in even number.");
        }

        StringJoiner joiner = new StringJoiner(", ");
        for (int i = 0; i < keyValues.length; i += 2) {
            String key = String.valueOf(keyValues[i]);
            String value = String.valueOf(keyValues[i + 1]);
            joiner.add(key + "=" + value);
        }
        return joiner.toString();
    }

    private static void record(Class<?> source,
                               String actorLabel,
                               String category,
                               String action,
                               String entity,
                               String status,
                               String details,
                               boolean warn) {
        Logger logger = LoggerFactory.getLogger(source == null ? AuditLogService.class : source);
        String message = buildMessage(category, action, entity, status, details);

        String previousUser = MDC.get("user");
        boolean overrideUser = actorLabel != null && !actorLabel.isBlank();
        if (overrideUser) {
            MDC.put("user", actorLabel);
        }

        try {
            if (warn) {
                logger.warn(message);
            } else {
                logger.info(message);
            }
        } finally {
            if (overrideUser) {
                if (previousUser == null) {
                    MDC.remove("user");
                } else {
                    MDC.put("user", previousUser);
                }
            }
        }
    }

    private static String buildMessage(String category, String action, String entity, String status, String details) {
        return PREFIX
                + "|category=" + encode(category)
                + "|action=" + encode(action)
                + "|entity=" + encode(entity)
                + "|status=" + encode(status)
                + "|details=" + encode(details);
    }

    private static String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private static String resolveActorLabel(User actor) {
        if (actor == null) {
            return "Guest";
        }
        if (actor.getEmail() != null && !actor.getEmail().isBlank()) {
            return actor.getEmail();
        }
        String fullName = actor.getFullName();
        if (fullName != null && !fullName.isBlank()) {
            return fullName;
        }
        if (actor.getId() != null) {
            return "User#" + actor.getId();
        }
        return "Authenticated user";
    }
}
