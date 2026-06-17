package nlu.fit.web.souvenirecommerce.core.filters;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class RootRedirectFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
        // No-op
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpRequest && response instanceof HttpServletResponse httpResponse) {
            String contextPath = httpRequest.getContextPath();
            String requestUri = httpRequest.getRequestURI();
            boolean isRootRequest = requestUri.equals(contextPath) || requestUri.equals(contextPath + "/");
            boolean isIndexRequest = requestUri.equals(contextPath + "/index.jsp");

            if (isRootRequest || isIndexRequest) {
                httpResponse.sendRedirect(contextPath + "/home");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // No-op
    }
}
