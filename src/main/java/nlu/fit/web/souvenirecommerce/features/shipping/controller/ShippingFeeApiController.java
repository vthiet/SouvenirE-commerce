package nlu.fit.web.souvenirecommerce.features.shipping.controller;

import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nlu.fit.web.souvenirecommerce.features.shipping.ShippingProvider;
import nlu.fit.web.souvenirecommerce.features.shipping.ShippingProviderRegistry;
import nlu.fit.web.souvenirecommerce.features.user.address.AddressService;
import nlu.fit.web.souvenirecommerce.model.entity.Address;
import nlu.fit.web.souvenirecommerce.model.entity.User;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Calculates the shipping fee for a given delivery address using the active provider.
 *
 * <p>URL: {@code GET /api/shipping/fee}
 *
 * <p>Parameters (one of the two groups must be provided):
 * <ul>
 *   <li>Saved address: {@code addressId}</li>
 *   <li>Inline address: {@code districtId} + {@code wardCode}</li>
 * </ul>
 */
@WebServlet("/api/shipping/fee")
public class ShippingFeeApiController extends HttpServlet {

    private final AddressService addressService = new AddressService();

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
            districtId = address.getEffectiveGhnDistrictId();
            wardCode = address.getEffectiveGhnWardCode();
        }

        if (districtId == null || wardCode == null || wardCode.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeError(response, "INVALID_CARRIER_ADDRESS");
            return;
        }

        String carrierCode = request.getParameter("carrier");
        ShippingProvider provider;
        if (carrierCode != null && !carrierCode.isBlank()) {
            try {
                provider = ShippingProviderRegistry.getByCode(carrierCode);
            } catch (IllegalArgumentException e) {
                provider = ShippingProviderRegistry.getDefault();
            }
        } else {
            provider = ShippingProviderRegistry.getDefault();
        }
        try {
            // Build a minimal address object so the provider can calculate the fee
            Address addr = new Address();
            addr.setCarrierDistrictId(districtId);
            addr.setCarrierWardCode(wardCode);

            BigDecimal shippingFee = provider.calculateFee(addr);
            JsonObject json = new JsonObject();
            json.addProperty("success", true);
            json.addProperty("shippingFee", shippingFee);
            json.addProperty("carrier", provider.getCode());
            json.addProperty("carrierName", provider.getName());
            response.getWriter().write(json.toString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeError(response, "SHIPPING_INTERRUPTED");
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
            JsonObject json = new JsonObject();
            json.addProperty("success", false);
            json.addProperty("shippingFee", 30000);
            json.addProperty("carrier", "FALLBACK");
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
