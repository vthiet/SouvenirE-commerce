package nlu.fit.web.souvenirecommerce.features.cart.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import nlu.fit.web.souvenirecommerce.core.logging.AuditLogService;
import nlu.fit.web.souvenirecommerce.features.cart.model.CartEntity;
import nlu.fit.web.souvenirecommerce.features.cart.service.CartService;
import nlu.fit.web.souvenirecommerce.model.entity.User;

import java.io.IOException;

@WebServlet("/cart/remove")
public class RemoveCartController extends HttpServlet {
    private final CartService cartService = new CartService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);
        boolean isAjax = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
        User currentUser = resolveCurrentUser(session);

        if (session == null) {
            AuditLogService.failure(
                    RemoveCartController.class,
                    currentUser,
                    "CART",
                    "CART_ITEM_REMOVED",
                    "CART",
                    AuditLogService.describe("reason", "session_missing")
            );
            handleResponse(response, isAjax, false, 0, 0);
            return;
        }

        try {
            Long productId = Long.parseLong(request.getParameter("productId"));
            boolean removed = cartService.removeItem(session, productId);
            CartEntity cart = cartService.getCartForDisplay(session);
            cartService.storeCart(session, cart);

            if (removed) {
                AuditLogService.success(
                        RemoveCartController.class,
                        currentUser,
                        "CART",
                        "CART_ITEM_REMOVED",
                        "CART",
                        AuditLogService.describe("productId", productId, "totalQuantity", cart.totalQuantity())
                );
            } else {
                AuditLogService.failure(
                        RemoveCartController.class,
                        currentUser,
                        "CART",
                        "CART_ITEM_REMOVED",
                        "CART",
                        AuditLogService.describe("productId", productId, "reason", "remove_failed")
                );
            }
            handleResponse(response, isAjax, removed, cart.totalQuantity(), cart.total());

        } catch (Exception e) {
            CartEntity cart = cartService.getCartForDisplay(session);
            AuditLogService.failure(
                    RemoveCartController.class,
                    currentUser,
                    "CART",
                    "CART_ITEM_REMOVED",
                    "CART",
                    AuditLogService.describe("reason", "exception", "message", e.getMessage())
            );
            handleResponse(response, isAjax, false, cart.totalQuantity(), cart.total());
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        doPost(request, response);
    }

    private void handleResponse(HttpServletResponse response,
                                boolean isAjax,
                                boolean success,
                                int totalQty,
                                double total) throws IOException {

        if (isAjax) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            String json = """
            {
              "success": %b,
              "totalQuantity": %d,
              "total": %.0f
            }
            """.formatted(success, totalQty, total);

            response.getWriter().write(json);
        } else {
            response.sendRedirect("/cart");
        }
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
