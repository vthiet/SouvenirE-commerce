package nlu.fit.web.souvenirecommerce.common.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public final class I18nUtil {

    private static final String BUNDLE_BASE_NAME = "messages";
    private static final Locale DEFAULT_LOCALE = Locale.forLanguageTag("vi");
    private static final ThreadLocal<Locale> THREAD_LOCALE = new ThreadLocal<>();

    private I18nUtil() {
    }

    public static void setThreadLocale(Locale locale) {
        if (locale == null) {
            THREAD_LOCALE.remove();
            return;
        }
        THREAD_LOCALE.set(locale);
    }

    public static void clearThreadLocale() {
        THREAD_LOCALE.remove();
    }

    public static Locale currentLocale() {
        Locale locale = THREAD_LOCALE.get();
        return locale == null ? DEFAULT_LOCALE : locale;
    }

    public static boolean isEnglishLocale() {
        return Locale.ENGLISH.getLanguage().equalsIgnoreCase(currentLocale().getLanguage());
    }

    public static Locale resolveLocale(HttpServletRequest request) {
        Object requestLocale = request.getAttribute("siteLocale");
        if (requestLocale instanceof Locale locale) {
            return locale;
        }

        HttpSession session = request.getSession(false);
        if (session != null) {
            Object sessionLocale = session.getAttribute("siteLocale");
            if (sessionLocale instanceof Locale locale) {
                return locale;
            }

            Object sessionLanguage = session.getAttribute("siteLanguage");
            if (sessionLanguage instanceof String language) {
                return normalizeLanguage(language);
            }
        }

        return DEFAULT_LOCALE;
    }

    public static String message(HttpServletRequest request, String key, Object... args) {
        return message(resolveLocale(request), key, args);
    }

    public static String message(Locale locale, String key, Object... args) {
        Locale effectiveLocale = locale == null ? DEFAULT_LOCALE : locale;
        ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_BASE_NAME, effectiveLocale);
        String pattern = bundle.containsKey(key) ? bundle.getString(key) : key;
        if (args == null || args.length == 0) {
            return pattern;
        }
        return MessageFormat.format(pattern, args);
    }

    private static Locale normalizeLanguage(String language) {
        if (language == null) {
            return DEFAULT_LOCALE;
        }

        String normalized = language.trim().toLowerCase(Locale.ROOT);
        if (normalized.startsWith("en")) {
            return Locale.ENGLISH;
        }
        return DEFAULT_LOCALE;
    }
}
