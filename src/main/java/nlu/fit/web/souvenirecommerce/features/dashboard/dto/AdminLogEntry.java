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
    private final String entryType;
    private final String category;
    private final String action;
    private final String entity;
    private final String status;
    private final String details;
    private final String displayMessage;

    public AdminLogEntry(String timestamp,
                         String thread,
                         String level,
                         String logger,
                         String requestId,
                         String user,
                         String message,
                         String rawLine,
                         String entryType,
                         String category,
                         String action,
                         String entity,
                         String status,
                         String details,
                         String displayMessage) {
        this.timestamp = timestamp;
        this.thread = thread;
        this.level = level;
        this.logger = logger;
        this.requestId = requestId;
        this.user = user;
        this.message = message;
        this.rawLine = rawLine;
        this.entryType = entryType;
        this.category = category;
        this.action = action;
        this.entity = entity;
        this.status = status;
        this.details = details;
        this.displayMessage = displayMessage;
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

    public String getEntryType() {
        return entryType;
    }

    public String getCategory() {
        return category;
    }

    public String getAction() {
        return action;
    }

    public String getEntity() {
        return entity;
    }

    public String getStatus() {
        return status;
    }

    public String getDetails() {
        return details;
    }

    public String getDisplayMessage() {
        return displayMessage;
    }

    public String getTypeLabel() {
        return "ACTIVITY".equalsIgnoreCase(entryType) ? "Activity" : "System";
    }

    public String getActionLabel() {
        return action == null || action.isBlank() ? "-" : action;
    }
}
