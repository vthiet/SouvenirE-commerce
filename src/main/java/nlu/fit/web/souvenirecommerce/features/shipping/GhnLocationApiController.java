package nlu.fit.web.souvenirecommerce.features.shipping;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/api/ghn/locations")
public class GhnLocationApiController extends HttpServlet {
    private final GhnService ghnService = new GhnService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        try {
            String type = request.getParameter("type");
            String body;

            if ("province".equals(type)) {
                body = ghnService.getProvinces();
            } else if ("district".equals(type)) {
                Integer provinceId = parseInteger(request.getParameter("provinceId"));
                if (provinceId == null) {
                    writeBadRequest(response, "INVALID_PROVINCE");
                    return;
                }
                body = ghnService.getDistricts(provinceId);
            } else if ("ward".equals(type)) {
                Integer districtId = parseInteger(request.getParameter("districtId"));
                if (districtId == null) {
                    writeBadRequest(response, "INVALID_DISTRICT");
                    return;
                }
                body = ghnService.getWards(districtId);
            } else {
                writeBadRequest(response, "INVALID_TYPE");
                return;
            }

            response.getWriter().write(body);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"GHN_INTERRUPTED\"}");
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
            response.getWriter().write("{\"error\":\"GHN_REQUEST_FAILED\"}");
        }
    }

    private void writeBadRequest(HttpServletResponse response, String error) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write("{\"error\":\"" + error + "\"}");
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
