package nlu.fit.web.souvenirecommerce.core.filters;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import nlu.fit.web.souvenirecommerce.common.utils.I18nUtil;
import nlu.fit.web.souvenirecommerce.features.cart.service.CartService;
import nlu.fit.web.souvenirecommerce.features.product.service.ICategoryService;
import nlu.fit.web.souvenirecommerce.features.product.service.impl.CategoryServiceImpl;
import nlu.fit.web.souvenirecommerce.legacy.dao.SettingsDAO;

import java.io.IOException;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

public class HeaderFilter implements Filter {

    private ICategoryService categoryService;
    private SettingsDAO settingsDAO;
    private CartService cartService;

    @Override
    public void init(FilterConfig filterConfig) {
        this.categoryService = new CategoryServiceImpl();
        this.settingsDAO = new SettingsDAO();
        this.cartService = new CartService();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpSession session = req.getSession(false);

        try {
            if (shouldSkip(req) || isAdminRequest(req)) {
                chain.doFilter(request, response);
                return;
            }

            Map<String, String> settings = loadSiteSettings();
            applyLocale(req, response, session, settings);
            request.setAttribute("currentRequestPath", buildRequestPath(req));
            setAuthUser(request, session);
            setHeaderCategories(request);
            setCartItemCount(request, session);
            request.setAttribute("settings", settings);

            chain.doFilter(request, response);
        } finally {
            I18nUtil.clearThreadLocale();
        }
    }

    private boolean shouldSkip(HttpServletRequest request) {
        String uri = request.getRequestURI();

        return uri.contains("/assets/") || uri.endsWith(".css") || uri.endsWith(".js") || uri.endsWith(".png") || uri.endsWith(".jpg") || uri.endsWith(".jpeg") || uri.endsWith(".webp") || uri.endsWith(".svg") || uri.endsWith(".ico") || uri.endsWith(".woff") || uri.endsWith(".woff2") || uri.endsWith(".ttf");
    }

    private boolean isAdminRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();

        if (uri == null) {
            return false;
        }

        String normalizedUri = uri;
        if (contextPath != null && !contextPath.isBlank() && normalizedUri.startsWith(contextPath)) {
            normalizedUri = normalizedUri.substring(contextPath.length());
        }

        return normalizedUri.equals("/admin") || normalizedUri.startsWith("/admin/");
    }

    private void setAuthUser(ServletRequest request, HttpSession session) {
        if (session == null) {
            return;
        }

        Object authUser = session.getAttribute("authUser");

        if (authUser == null) {
            authUser = session.getAttribute("userInSession");
        }

        if (authUser != null) {
            request.setAttribute("authUser", authUser);
        }
    }

    private void setHeaderCategories(ServletRequest request) {
        try {
            request.setAttribute("headerCategories", categoryService.getHeaderCategories());

            request.setAttribute("headerTopCategories", categoryService.getTopSellingHeaderCategories(5));
        } catch (RuntimeException e) {
            e.printStackTrace();

            request.setAttribute("headerCategories", Collections.emptyList());

            request.setAttribute("headerTopCategories", Collections.emptyList());
        }
    }

    private void setCartItemCount(ServletRequest request, HttpSession session) {
        if (session == null) {
            request.setAttribute("cartItemCount", 0);
            return;
        }

        int cartItemCount = cartService.totalQuantity(session);
        request.setAttribute("cartItemCount", cartItemCount);
    }

    private Map<String, String> loadSiteSettings() {
        try {
            return settingsDAO.getAllSettings();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }

    private String buildRequestPath(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();

        if (requestUri == null) {
            return "/";
        }

        if (contextPath != null && !contextPath.isEmpty() && requestUri.startsWith(contextPath)) {
            return requestUri.substring(contextPath.length());
        }

        return requestUri;
    }

    private void applyLocale(HttpServletRequest request, ServletResponse response, HttpSession session, Map<String, String> settings) {
        String requestedLanguage = normalizeLanguage(request.getParameter("lang"));
        String sessionLanguage = session == null ? null : normalizeLanguage((String) session.getAttribute("siteLanguage"));
        String defaultLanguage = normalizeLanguage(settings.get("default_language"));

        String language = firstNonBlank(requestedLanguage, sessionLanguage, defaultLanguage, "vi");
        Locale locale = "en".equals(language) ? Locale.ENGLISH : Locale.forLanguageTag("vi");

        request.setAttribute("siteLanguage", language);
        request.setAttribute("siteLocale", locale);
        I18nUtil.setThreadLocale(locale);

        if (response instanceof jakarta.servlet.http.HttpServletResponse httpResponse) {
            httpResponse.setLocale(locale);
        }

        if (requestedLanguage != null || session != null) {
            HttpSession targetSession = session != null ? session : request.getSession();
            targetSession.setAttribute("siteLanguage", language);
            targetSession.setAttribute("siteLocale", locale);
        }
    }

    private String normalizeLanguage(String language) {
        if (language == null || language.isBlank()) {
            return null;
        }

        String normalized = language.trim().toLowerCase(Locale.ROOT);
        if (normalized.startsWith("vi")) {
            return "vi";
        }
        if (normalized.startsWith("en")) {
            return "en";
        }
        return null;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    @Override
    public void destroy() {
    }
}
