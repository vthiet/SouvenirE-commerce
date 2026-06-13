package nlu.fit.web.souvenirecommerce.model.enums;

public enum EmailType {

    PLAIN_TEXT("text/plain; charset=UTF-8"),

    HTML("text/html; charset=UTF-8"),

    MULTIPART_MIXED("multipart/mixed"),

    MULTIPART_ALTERNATIVE("multipart/alternative");

    private final String mimeType;

    EmailType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getMimeType() {
        return mimeType;
    }
}