package nlu.fit.web.souvenirecommerce.features.payment;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import jakarta.servlet.http.HttpServletRequest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class VnPayUtil {

    public static String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(
                    key.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA512"
            );
            hmac512.init(secretKey);
            byte[] bytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder hash = new StringBuilder();
            for (byte b : bytes) {
                hash.append(String.format("%02x", b));
            }
            return hash.toString();
        } catch (Exception e) {
            throw new RuntimeException("Cannot generate HMAC SHA512", e);
        }
    }

    public static String buildQueryString(Map<String, String> params, boolean encodeValue) {
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        StringBuilder query = new StringBuilder();

        for (String fieldName : fieldNames) {
            String fieldValue = params.get(fieldName);

            if (fieldValue != null && !fieldValue.isEmpty()) {
                if (!query.isEmpty()) {
                    query.append("&");
                }

                query.append(urlEncode(fieldName));
                query.append("=");

                if (encodeValue) {
                    query.append(urlEncode(fieldValue));
                } else {
                    query.append(fieldValue);
                }
            }
        }

        return query.toString();
    }

    public static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    public static String getClientIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-FORWARDED-FOR");

        if (ipAddress == null || ipAddress.isBlank()) {
            ipAddress = request.getRemoteAddr();
        }

        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }

        return ipAddress;
    }

    public static boolean verifySignature(Map<String, String> fields, String hashSecret) {
        String receivedHash = fields.get("vnp_SecureHash");

        if (receivedHash == null || receivedHash.isBlank()) {
            return false;
        }

        Map<String, String> signedFields = new HashMap<>(fields);
        signedFields.remove("vnp_SecureHash");
        signedFields.remove("vnp_SecureHashType");

        String signData = buildQueryString(signedFields, true);
        String calculatedHash = hmacSHA512(hashSecret, signData);

        return calculatedHash.equalsIgnoreCase(receivedHash);
    }

    public static Map<String, String> getRequestParams(HttpServletRequest request) {
        Map<String, String> fields = new HashMap<>();

        Enumeration<String> parameterNames = request.getParameterNames();

        while (parameterNames.hasMoreElements()) {
            String name = parameterNames.nextElement();
            String value = request.getParameter(name);

            if (value != null && !value.isBlank()) {
                fields.put(name, value);
            }
        }

        return fields;
    }
}
