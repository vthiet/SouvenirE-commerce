package nlu.fit.web.souvenirecommerce.features.payment;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VnPayUtilTest {
    @Test
    void verifiesValidSignatureAndRejectsChangedPayload() {
        String secret = "test-secret";
        Map<String, String> fields = new HashMap<>();
        fields.put("vnp_Amount", "12500000");
        fields.put("vnp_OrderInfo", "Thanh toan don hang 42");
        fields.put("vnp_TxnRef", "42");
        fields.put("vnp_SecureHash", VnPayUtil.hmacSHA512(secret, VnPayUtil.buildQueryString(fields, true)));

        assertTrue(VnPayUtil.verifySignature(fields, secret));

        fields.put("vnp_Amount", "100");
        assertFalse(VnPayUtil.verifySignature(fields, secret));
    }
}
