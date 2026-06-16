package nlu.fit.web.souvenirecommerce.common.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public final class RecaptchaUtil {
    private static final String VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";
    private static final String SITE_KEY_PARAM = "recaptcha.siteKey";
    private static final String SECRET_KEY_PARAM = "recaptcha.secretKey";
    private static final String SITE_KEY_ENV = "RECAPTCHA_SITE_KEY";
    private static final String SECRET_KEY_ENV = "RECAPTCHA_SECRET_KEY";
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private RecaptchaUtil() {
    }

    public static String getSiteKey(ServletContext context) {
        return getConfig(context, SITE_KEY_PARAM, SITE_KEY_ENV);
    }

    public static boolean isConfigured(ServletContext context) {
        return !getSiteKey(context).isBlank() && !getSecretKey(context).isBlank();
    }

    public static boolean verify(HttpServletRequest request, ServletContext context) {
        if (!isConfigured(context)) {
            return true;
        }

        String token = request.getParameter("g-recaptcha-response");
        if (token == null || token.isBlank()) {
            return false;
        }

        String body = "secret=" + urlEncode(getSecretKey(context))
                + "&response=" + urlEncode(token)
                + "&remoteip=" + urlEncode(request.getRemoteAddr());

        HttpRequest verifyRequest = HttpRequest.newBuilder(URI.create(VERIFY_URL))
                .timeout(Duration.ofSeconds(8))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        try {
            HttpResponse<String> response = CLIENT.send(verifyRequest, HttpResponse.BodyHandlers.ofString());
            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            return json.has("success") && json.get("success").getAsBoolean();
        } catch (IOException | InterruptedException | RuntimeException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return false;
        }
    }

    public static void expose(HttpServletRequest request, ServletContext context) {
        request.setAttribute("recaptchaSiteKey", getSiteKey(context));
        request.setAttribute("recaptchaConfigured", isConfigured(context));
    }

    private static String getSecretKey(ServletContext context) {
        return getConfig(context, SECRET_KEY_PARAM, SECRET_KEY_ENV);
    }

    private static String getConfig(ServletContext context, String key, String envName) {
        String value = ApplicationLoader.get(key);
        if (value == null || value.isBlank()) {
            value = context == null ? null : context.getInitParameter(key);
        }
        if (value == null || value.isBlank()) {
            value = System.getProperty(key);
        }
        if (value == null || value.isBlank()) {
            value = System.getProperty(envName);
        }
        if (value == null || value.isBlank()) {
            value = System.getenv(envName);
        }
        return value == null ? "" : value.trim();
    }

    private static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
