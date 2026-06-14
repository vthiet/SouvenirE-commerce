package nlu.fit.web.souvenirecommerce.core.filters;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nlu.fit.web.souvenirecommerce.common.utils.AdminAccessHelper;

import java.io.IOException;

public class AdminAccessFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
        // No initialization required.
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String servletPath = req.getServletPath();

        if ("/admin/access-denied.jsp".equals(servletPath)) {
            chain.doFilter(request, response);
            return;
        }

        res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        res.setHeader("Pragma", "no-cache");
        res.setDateHeader("Expires", 0);

        if (!AdminAccessHelper.hasAdminAccess(req)) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            req.setAttribute("accessDeniedTitle", "Không có quyền truy cập");
            req.setAttribute(
                    "accessDeniedMessage",
                    "Bạn cần vai trò Sales, Admin hoặc Super Admin, hoặc quyền dashboard.read để mở trang quản trị này."
            );
            req.getRequestDispatcher("/admin/access-denied.jsp").forward(req, res);
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // No resources to release.
    }
}
