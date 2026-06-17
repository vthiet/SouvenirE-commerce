package nlu.fit.web.souvenirecommerce.features.shipping.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nlu.fit.web.souvenirecommerce.features.shipping.ShippingProviderRegistry;

import java.io.IOException;

/**
 * @deprecated Use {@link ShippingLocationApiController} at {@code /api/shipping/locations} instead.
 * This servlet is kept for backward-compatibility with any bookmarked URLs and
 * will be removed in a future release.
 */
@Deprecated
@WebServlet("/api/ghn/locations")
public class GhnLocationApiController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Delegate entirely to the new provider-agnostic controller logic
        response.setContentType("application/json;charset=UTF-8");
        try {
            String type     = request.getParameter("type");
            String parentId = null;
            if ("district".equals(type)) {
                parentId = request.getParameter("provinceId");
            } else if ("ward".equals(type)) {
                parentId = request.getParameter("districtId");
            }
            String body = ShippingProviderRegistry.getDefault().getLocations(type, parentId);
            response.getWriter().write(body);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"INTERRUPTED\"}");
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
            response.getWriter().write("{\"error\":\"REQUEST_FAILED\"}");
        }
    }
}
