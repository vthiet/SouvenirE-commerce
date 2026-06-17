package nlu.fit.web.souvenirecommerce.features.shipping.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nlu.fit.web.souvenirecommerce.features.shipping.ShippingProviderRegistry;

import java.io.IOException;

/**
 * Provider-agnostic API for fetching location data (provinces / districts / wards).
 *
 * <p>URL: {@code GET /api/shipping/locations}
 *
 * <p>Parameters:
 * <ul>
 *   <li>{@code type} — {@code "province"}, {@code "district"}, or {@code "ward"}</li>
 *   <li>{@code provinceId} — required when type is {@code "district"}</li>
 *   <li>{@code districtId} — required when type is {@code "ward"}</li>
 * </ul>
 *
 * <p>The response JSON format is determined by the active provider so that
 * existing front-end code does not need to change when switching carriers.
 */
@WebServlet("/api/shipping/locations")
public class ShippingLocationApiController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        try {
            String type = request.getParameter("type");
            String parentId = resolveParentId(type, request);
            String body = ShippingProviderRegistry.getDefault().getLocations(type, parentId);
            response.getWriter().write(body);
        } catch (IllegalArgumentException | IllegalStateException e) {
            writeBadRequest(response, "INVALID_REQUEST", e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"SHIPPING_INTERRUPTED\"}");
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
            response.getWriter().write("{\"error\":\"SHIPPING_REQUEST_FAILED\",\"message\":\""
                    + escapeJson(e.getMessage()) + "\"}");
        }
    }

    /**
     * Resolves the {@code parentId} string from the appropriate query parameter
     * depending on the requested location type.
     */
    private String resolveParentId(String type, HttpServletRequest request) {
        if ("district".equals(type)) {
            return request.getParameter("provinceId");
        }
        if ("ward".equals(type)) {
            return request.getParameter("districtId");
        }
        return null;
    }

    private void writeBadRequest(HttpServletResponse response, String error, String detail) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write("{\"error\":\"" + error + "\",\"message\":\"" + escapeJson(detail) + "\"}");
    }

    private String escapeJson(String value) {
        if (value == null || value.isBlank()) {
            return "Không thể kết nối đơn vị vận chuyển.";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "")
                .replace("\n", " ");
    }
}
