package nlu.fit.web.souvenirecommerce.common.utils;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Locale;

public final class AuthorizationPolicy {
    private AuthorizationPolicy() {
    }

    public record RequiredPermission(String resource, String action, boolean protectedRoute) {
    }

    public static RequiredPermission resolve(HttpServletRequest request) {
        String servletPath = request.getServletPath();
        if (servletPath == null || !servletPath.startsWith("/admin")) {
            return new RequiredPermission(null, null, false);
        }

        if ("/admin".equals(servletPath) || "/admin/".equals(servletPath) || "/admin/dashboard".equals(servletPath)) {
            return new RequiredPermission("dashboard", "read", true);
        }

        if ("/admin/logs".equals(servletPath)) {
            return new RequiredPermission("log", "read", true);
        }

        if ("/admin/products".equals(servletPath)) {
            return resolveCrudPermission(request, "product", "read");
        }

        if ("/admin/categories".equals(servletPath)) {
            return resolveCrudPermission(request, "category", "read");
        }

        if ("/admin/orders".equals(servletPath)) {
            return resolveCrudPermission(request, "order", "read");
        }

        if ("/admin/customers".equals(servletPath)) {
            return resolveCrudPermission(request, "customer", "read");
        }

        if ("/admin/banners".equals(servletPath) || "/admin/banner".equals(servletPath)) {
            return resolveCrudPermission(request, "banner", "read");
        }

        if ("/admin/settings".equals(servletPath)) {
            return "POST".equalsIgnoreCase(request.getMethod())
                    ? new RequiredPermission("settings", "update", true)
                    : new RequiredPermission("settings", "read", true);
        }

        if ("/admin/roles".equals(servletPath)) {
            if ("POST".equalsIgnoreCase(request.getMethod())) {
                String action = safeLower(request.getParameter("action"));
                String mappedAction = switch (action) {
                    case "add", "create", "save" -> "create";
                    case "edit", "update" -> "update";
                    case "delete", "remove" -> "delete";
                    case "assignusers", "assign", "syncusers" -> "update";
                    default -> "update";
                };
                return new RequiredPermission("role", mappedAction, true);
            }
            return new RequiredPermission("role", "read", true);
        }

        return new RequiredPermission("admin", "read", true);
    }

    private static RequiredPermission resolveCrudPermission(HttpServletRequest request, String resource, String defaultAction) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return new RequiredPermission(resource, defaultAction, true);
        }

        String action = safeLower(request.getParameter("action"));
        String mappedAction = switch (action) {
            case "add", "create", "insert" -> "create";
            case "edit", "update", "save" -> "update";
            case "delete", "remove" -> "delete";
            case "togglestatus", "approve", "reject" -> "update";
            default -> defaultAction;
        };

        return new RequiredPermission(resource, mappedAction, true);
    }

    private static String safeLower(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
