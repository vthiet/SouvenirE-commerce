package nlu.fit.web.souvenirecommerce.features.cart.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import nlu.fit.web.souvenirecommerce.core.logging.AuditLogService;
import nlu.fit.web.souvenirecommerce.features.cart.model.CartEntity;
import nlu.fit.web.souvenirecommerce.features.cart.model.CartItemEntity;
import nlu.fit.web.souvenirecommerce.features.cart.service.CartService;
import nlu.fit.web.souvenirecommerce.model.entity.User;

import java.io.IOException;

@WebServlet("/cart/update")
public class UpdateCartController extends HttpServlet {
    private final CartService cartService = new CartService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        User currentUser = resolveCurrentUser(session);

        try {
            Long productId = Long.parseLong(request.getParameter("productId"));
            int quantity   = Integer.parseInt(request.getParameter("quantity"));

            if (session == null) {
                session = request.getSession();
            }
            boolean updated = cartService.updateItem(session, productId, quantity);
            if (!updated) {
                AuditLogService.failure(
                        UpdateCartController.class,
                        currentUser,
                        "CART",
                        "CART_ITEM_UPDATED",
                        "CART",
                        AuditLogService.describe("productId", productId, "quantity", quantity, "reason", "update_failed")
                );
                response.getWriter().write("{\"success\":false}");
                return;
            }

            CartEntity cart = cartService.getCartForDisplay(session);
            cartService.storeCart(session, cart);

            CartItemEntity item = cart.getItem(productId);
            double itemSubtotal = (item != null) ? item.getSubTotal() : 0;

            String json = """
            {
              "success": true,
              "totalQuantity": %d,
              "total": %.0f,
              "itemSubtotal": %.0f
            }
            """.formatted(
                    cart.totalQuantity(),
                    cart.total(),
                    itemSubtotal
            );

            AuditLogService.success(
                    UpdateCartController.class,
                    currentUser,
                    "CART",
                    "CART_ITEM_UPDATED",
                    "CART",
                    AuditLogService.describe(
                            "productId", productId,
                            "quantity", quantity,
                            "totalQuantity", cart.totalQuantity(),
                            "total", cart.total()
                    )
            );
            response.getWriter().write(json);

        } catch (Exception e) {
            AuditLogService.failure(
                    UpdateCartController.class,
                    currentUser,
                    "CART",
                    "CART_ITEM_UPDATED",
                    "CART",
                    AuditLogService.describe("reason", "exception", "message", e.getMessage())
            );
            response.getWriter().write("{\"success\":false}");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    private User resolveCurrentUser(HttpSession session) {
        if (session == null) {
            return null;
        }
        User currentUser = (User) session.getAttribute("userInSession");
        if (currentUser == null) {
            currentUser = (User) session.getAttribute("currentUser");
        }
        if (currentUser == null) {
            currentUser = (User) session.getAttribute("user");
        }
        return currentUser;
    }
}
