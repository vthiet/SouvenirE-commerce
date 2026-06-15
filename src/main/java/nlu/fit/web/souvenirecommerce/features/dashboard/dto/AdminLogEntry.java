package nlu.fit.web.souvenirecommerce.features.dashboard.dto;

public class AdminLogEntry {
    private final String timestamp;
    private final String thread;
    private final String level;
    private final String logger;
    private final String requestId;
    private final String user;
    private final String message;
    private final String rawLine;

    public AdminLogEntry(String timestamp, String thread, String level, String logger, String requestId, String user, String message, String rawLine) {
        this.timestamp = timestamp;
        this.thread = thread;
        this.level = level;
        this.logger = logger;
        this.requestId = requestId;
        this.user = user;
        this.message = message;
        this.rawLine = rawLine;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getThread() {
        return thread;
    }

    public String getLevel() {
        return level;
    }

    public String getLogger() {
        return logger;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getUser() {
        return user;
    }

    public String getMessage() {
        return message;
    }

    public String getRawLine() {
        return rawLine;
    }
}
