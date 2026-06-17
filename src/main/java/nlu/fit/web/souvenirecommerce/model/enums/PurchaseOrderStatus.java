package nlu.fit.web.souvenirecommerce.model.enums;

import lombok.Getter;

@Getter
public enum PurchaseOrderStatus {
    DRAFT("Nháp"),
    FINALIZED("Đã nhập kho"),
    CANCELLED("Đã hủy");

    private final String description;

    PurchaseOrderStatus(String description) {
        this.description = description;
    }

    public boolean isDraft() {
        return this == DRAFT;
    }

    public boolean isFinalized() {
        return this == FINALIZED;
    }

    public boolean isCancelled() {
        return this == CANCELLED;
    }

    public static PurchaseOrderStatus fromCode(String value) {
        if (value == null || value.isBlank()) {
            return FINALIZED;
        }

        try {
            return PurchaseOrderStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return FINALIZED;
        }
    }
}
