package nlu.fit.web.souvenirecommerce.features.cart.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import nlu.fit.web.souvenirecommerce.common.utils.GsonUtil;
import nlu.fit.web.souvenirecommerce.features.cart.model.Cart;
import nlu.fit.web.souvenirecommerce.features.cart.service.CartPersistenceService;
import nlu.fit.web.souvenirecommerce.features.cart.service.CartSummaryService;
import nlu.fit.web.souvenirecommerce.model.entity.User;

import java.io.IOException;

@WebServlet("/cart/summary")
public class CartSummaryController extends HttpServlet {

    private final CartPersistenceService cartPersistenceService = new CartPersistenceService();
    private final CartSummaryService cartSummaryService = new CartSummaryService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);
        Cart cart = null;

        if (session != null) {
            cart = (Cart) session.getAttribute("cart");

            if (cart == null) {
                User user = getCurrentUser(session);

                if (user != null) {
                    cart = cartPersistenceService.loadCart(user);
                    session.setAttribute("cart", cart);
                    session.setAttribute("cartItemCount", cart.totalQuantity());
                }
            }
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(GsonUtil.getGson().toJson(
                cartSummaryService.buildSummary(cart, request.getContextPath())
        ));
    }

    private User getCurrentUser(HttpSession session) {
        Object user = session.getAttribute("userInSession");

        if (user instanceof User currentUser) {
            return currentUser;
        }

        user = session.getAttribute("authUser");

        if (user instanceof User currentUser) {
            return currentUser;
        }

        return null;
    }
}
