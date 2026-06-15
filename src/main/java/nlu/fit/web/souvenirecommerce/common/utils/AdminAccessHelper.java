package nlu.fit.web.souvenirecommerce.common.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import nlu.fit.web.souvenirecommerce.legacy.model.PermissionGroup;

public final class AdminAccessHelper {

    private static final Set<String> ADMIN_ROLES = Set.of(
            "Sales",
            "Admin",
            "Super Admin",
            "SuperAdmin"
    );

    private AdminAccessHelper() {
    }

    public static boolean hasAdminAccess(HttpServletRequest request) {
        if (request == null) {
            return false;
        }

        if (PermissionHelper.hasPermission(request, "dashboard", "read")) {
            return true;
        }

        if (hasAdminRoleFromPermissions(request)) {
            return true;
        }

        for (String role : ADMIN_ROLES) {
            if (request.isUserInRole(role)) {
                return true;
            }
        }

        HttpSession session = request.getSession(false);
        return session != null && hasAdminRoleFromSession(session);
    }

    private static boolean hasAdminRoleFromPermissions(HttpServletRequest request) {
        for (PermissionGroup group : PermissionHelper.getUserRoles(request)) {
            if (group != null && isAdminRole(group.getName())) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasAdminRoleFromSession(HttpSession session) {
        Set<Object> visited = new LinkedHashSet<>();
        for (String attributeName : new String[]{"user", "userInSession"}) {
            Object attribute = session.getAttribute(attributeName);
            if (matchesAdminRole(attribute, visited, 0)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesAdminRole(Object value, Set<Object> visited, int depth) {
        if (value == null || depth > 3 || visited.contains(value)) {
            return false;
        }
        visited.add(value);

        if (value instanceof CharSequence sequence) {
            return isAdminRole(sequence.toString());
        }

        if (value instanceof Collection<?> collection) {
            for (Object item : collection) {
                if (matchesAdminRole(item, visited, depth + 1)) {
                    return true;
                }
            }
            return false;
        }

        Class<?> type = value.getClass();
        if (type.isArray()) {
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
                if (matchesAdminRole(Array.get(value, i), visited, depth + 1)) {
                    return true;
                }
            }
            return false;
        }

        for (String methodName : new String[]{
                "getRole",
                "getRoleName",
                "getName",
                "getAuthority",
                "getAuthorities",
                "getRoles"
        }) {
            Object result = invokeNoArg(value, methodName);
            if (result != null && result != value && matchesAdminRole(result, visited, depth + 1)) {
                return true;
            }
        }

        return false;
    }

    private static Object invokeNoArg(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            method.setAccessible(true);
            return method.invoke(target);
        } catch (ReflectiveOperationException ex) {
            return null;
        }
    }

    private static boolean isAdminRole(String value) {
        if (value == null) {
            return false;
        }

        String normalized = normalizeRole(value);
        for (String role : ADMIN_ROLES) {
            if (normalizeRole(role).equals(normalized)) {
                return true;
            }
        }
        return false;
    }

    private static String normalizeRole(String value) {
        return value == null ? "" : value.trim().replaceAll("[\\s_-]+", "").toLowerCase(Locale.ROOT);
    }
}
