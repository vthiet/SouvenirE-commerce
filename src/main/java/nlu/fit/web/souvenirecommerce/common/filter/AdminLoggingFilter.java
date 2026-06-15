package nlu.fit.web.souvenirecommerce.common.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@WebFilter(urlPatterns = "/admin/*")
public class AdminLoggingFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(AdminLoggingFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!(request instanceof HttpServletRequest httpRequest) || !(response instanceof HttpServletResponse httpResponse)) {
            chain.doFilter(request, response);
            return;
        }

        long startedAt = System.currentTimeMillis();
        StatusCaptureResponseWrapper wrappedResponse = new StatusCaptureResponseWrapper(httpResponse);

        String method = httpRequest.getMethod();
        String uri = httpRequest.getRequestURI();
        String queryString = httpRequest.getQueryString();
        String user = httpRequest.getRemoteUser();

        log.info("Admin request started. method={}, uri={}, query={}, user={}",
                method, uri, queryString == null ? "-" : queryString, user == null ? "anonymous" : user);

        try {
            chain.doFilter(request, wrappedResponse);
            long durationMs = System.currentTimeMillis() - startedAt;
            log.info("Admin request finished. method={}, uri={}, status={}, durationMs={}",
                    method, uri, wrappedResponse.getStatus(), durationMs);
        } catch (IOException | ServletException ex) {
            long durationMs = System.currentTimeMillis() - startedAt;
            log.error("Admin request failed. method={}, uri={}, durationMs={}",
                    method, uri, durationMs, ex);
            throw ex;
        }
    }

    private static final class StatusCaptureResponseWrapper extends HttpServletResponseWrapper {
        private int status = SC_OK;

        private StatusCaptureResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        @Override
        public void setStatus(int sc) {
            super.setStatus(sc);
            this.status = sc;
        }

        @Override
        public void sendError(int sc) throws IOException {
            super.sendError(sc);
            this.status = sc;
        }

        @Override
        public void sendError(int sc, String msg) throws IOException {
            super.sendError(sc, msg);
            this.status = sc;
        }

        @Override
        public void sendRedirect(String location) throws IOException {
            super.sendRedirect(location);
            this.status = SC_FOUND;
        }

        @Override
        public int getStatus() {
            return status;
        }
    }
}
