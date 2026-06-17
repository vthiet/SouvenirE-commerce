package nlu.fit.web.souvenirecommerce.features.payment.port;

import java.math.BigDecimal;
import java.util.Map;

public interface PaymentProviderAdapter {
    /**
     * Create payment URL for redirecting user to the provider gateway.
     */
    String createPaymentUrl(Long transactionId, Long orderId, BigDecimal amount, String clientIp, String returnUrl);

    /**
     * Verify the parameters returned by the provider (usually from IPN webhook or Return URL).
     * Returns true if the signature is valid.
     */
    boolean verifySignature(Map<String, String> params);

    /**
     * Check if the response parameters indicate a successful payment.
     */
    boolean isPaymentSuccess(Map<String, String> params);

    /**
     * Extract the transaction ID of our system from the parameters.
     */
    Long getTransactionId(Map<String, String> params);

    /**
     * Extract the provider's transaction reference.
     */
    String getProviderTransactionRef(Map<String, String> params);

    /**
     * Extract response code.
     */
    String getResponseCode(Map<String, String> params);

    /**
     * Extract bank code.
     */
    String getBankCode(Map<String, String> params);

    /**
     * Extract amount from response to verify with DB.
     */
    BigDecimal getAmount(Map<String, String> params);
}
