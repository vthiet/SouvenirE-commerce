package nlu.fit.web.souvenirecommerce.features.shipping;

import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nlu.fit.web.souvenirecommerce.features.user.address.AddressService;
import nlu.fit.web.souvenirecommerce.model.entity.Address;
import nlu.fit.web.souvenirecommerce.model.entity.User;

import java.io.IOException;
import java.math.BigDecimal;

@WebServlet("/api/shipping-fee")
public class ShippingFeeApiController extends HttpServlet {
    private final AddressService addressService = new AddressService();
    private final GhnService ghnService = new GhnService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        User user = getCurrentUser(request);
        if (user == null || user.getId() == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writeError(response, "UNAUTHORIZED");
            return;
        }

        Integer districtId = parseInteger(request.getParameter("districtId"));
        String wardCode = request.getParameter("wardCode");

        Long addressId = parseLong(request.getParameter("addressId"));
        if (addressId != null) {
            Address address = addressService.getUserAddress(user.getId(), addressId).orElse(null);
            if (address == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                writeError(response, "ADDRESS_NOT_FOUND");
                return;
            }
            districtId = address.getGhnDistrictId();
            wardCode = address.getGhnWardCode();
        }

        if (districtId == null || wardCode == null || wardCode.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeError(response, "INVALID_GHN_ADDRESS");
            return;
        }

        try {
            BigDecimal shippingFee = ghnService.calculateFee(districtId, wardCode);
            JsonObject json = new JsonObject();
            json.addProperty("success", true);
            json.addProperty("shippingFee", shippingFee);
            json.addProperty("source", ghnService.isSimulation() ? "SIMULATION" : "GHN");
            response.getWriter().write(json.toString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeError(response, "GHN_INTERRUPTED");
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
            JsonObject json = new JsonObject();
            json.addProperty("success", false);
            json.addProperty("shippingFee", 30000);
            json.addProperty("source", "FALLBACK");
            json.addProperty("error", e.getMessage());
            response.getWriter().write(json.toString());
        }
    }

    private User getCurrentUser(HttpServletRequest request) {
        var session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object user = session.getAttribute("userInSession");
        if (user instanceof User currentUser) {
            return currentUser;
        }
        user = session.getAttribute("authUser");
        return user instanceof User currentUser ? currentUser : null;
    }

    private void writeError(HttpServletResponse response, String error) throws IOException {
        JsonObject json = new JsonObject();
        json.addProperty("success", false);
        json.addProperty("error", error);
        response.getWriter().write(json.toString());
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

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
