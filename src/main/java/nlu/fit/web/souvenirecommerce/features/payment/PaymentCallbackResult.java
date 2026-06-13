package nlu.fit.web.souvenirecommerce.features.payment;

import lombok.Builder;
import lombok.Getter;
import nlu.fit.web.souvenirecommerce.model.entity.PaymentTransaction;

@Getter
@Builder
public class PaymentCallbackResult {
    public enum Outcome {
        PROCESSED,
        ALREADY_PROCESSED,
        ORDER_NOT_FOUND,
        INVALID_AMOUNT,
        INVALID_REQUEST
    }

    private final Outcome outcome;
    private final boolean successful;
    private final PaymentTransaction transaction;

    public String getIpnResponseCode() {
        return switch (outcome) {
            case PROCESSED -> "00";
            case ORDER_NOT_FOUND -> "01";
            case ALREADY_PROCESSED -> "02";
            case INVALID_AMOUNT -> "04";
            case INVALID_REQUEST -> "99";
        };
    }
}
