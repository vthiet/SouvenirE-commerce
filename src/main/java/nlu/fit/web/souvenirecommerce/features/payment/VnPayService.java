package nlu.fit.web.souvenirecommerce.features.payment;

import nlu.fit.web.souvenirecommerce.common.utils.ApplicationLoader;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class VnPayService {
    private static final String VERSION = "2.1.0";
    private static final String COMMAND = "pay";
    private static final String CURRENCY = "VND";
    private static final String LOCALE = "vn";
    private static final String ORDER_TYPE = "other";
    private static final DateTimeFormatter VNPAY_DATE = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final String tmnCode = value("vnpay.tmnCode");
    private final String hashSecret = value("vnpay.hashSecret");
    private final String payUrl = value("vnpay.payUrl");
    private final String configuredReturnUrl = value("vnpay.returnUrl");

    public boolean isConfigured() {
        return isRealValue(tmnCode)
                && isRealValue(hashSecret)
                && isRealValue(payUrl);
    }

    public String createPaymentUrl(Long orderId, long amountVnd, String clientIp, String returnUrl) {
        if (!isConfigured()) {
            throw new IllegalStateException("VNPay configuration is missing");
        }
        if (orderId == null || amountVnd <= 0) {
            throw new IllegalArgumentException("Order id and amount must be valid");
        }

        LocalDateTime now = LocalDateTime.now();
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", VERSION);
        params.put("vnp_Command", COMMAND);
        params.put("vnp_TmnCode", tmnCode);
        params.put("vnp_Amount", String.valueOf(Math.multiplyExact(amountVnd, 100)));
        params.put("vnp_CurrCode", CURRENCY);
        params.put("vnp_TxnRef", String.valueOf(orderId));
        params.put("vnp_OrderInfo", "Thanh toan don hang " + orderId);
        params.put("vnp_OrderType", ORDER_TYPE);
        params.put("vnp_Locale", LOCALE);
        params.put("vnp_ReturnUrl", resolveReturnUrl(returnUrl));
        params.put("vnp_IpAddr", clientIp == null || clientIp.isBlank() ? "127.0.0.1" : clientIp);
        params.put("vnp_CreateDate", now.format(VNPAY_DATE));
        params.put("vnp_ExpireDate", now.plusMinutes(15).format(VNPAY_DATE));

        String signData = VnPayUtil.buildQueryString(params, true);
        return payUrl + "?" + signData + "&vnp_SecureHash=" + VnPayUtil.hmacSHA512(hashSecret, signData);
    }

    public String getHashSecret() {
        return hashSecret;
    }

    private String resolveReturnUrl(String dynamicReturnUrl) {
        if (isLocalUrl(configuredReturnUrl) && isRealValue(dynamicReturnUrl)) {
            return dynamicReturnUrl;
        }
        return isRealValue(configuredReturnUrl) ? configuredReturnUrl : dynamicReturnUrl;
    }

    private static boolean isLocalUrl(String value) {
        if (!isRealValue(value)) {
            return false;
        }
        try {
            String host = URI.create(value).getHost();
            return "localhost".equalsIgnoreCase(host)
                    || "127.0.0.1".equals(host)
                    || "::1".equals(host);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static String value(String key) {
        String value = ApplicationLoader.get(key);
        return value == null ? "" : value.trim();
    }

    private static boolean isRealValue(String value) {
        return value != null
                && !value.isBlank()
                && !value.startsWith("YOUR_");
    }
}
