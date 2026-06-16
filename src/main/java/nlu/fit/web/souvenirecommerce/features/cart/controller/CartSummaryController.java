package nlu.fit.web.souvenirecommerce.features.cart.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import nlu.fit.web.souvenirecommerce.common.utils.GsonUtil;
import nlu.fit.web.souvenirecommerce.features.cart.model.CartEntity;
import nlu.fit.web.souvenirecommerce.features.cart.service.CartService;
import nlu.fit.web.souvenirecommerce.features.cart.service.CartSummaryService;

import java.io.IOException;

@WebServlet("/cart/summary")
public class CartSummaryController extends HttpServlet {

    private final CartService cartService = new CartService();
    private final CartSummaryService cartSummaryService = new CartSummaryService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(true);
        CartEntity cart = cartService.getCartForDisplay(session);
        cartService.storeCart(session, cart);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(GsonUtil.getGson().toJson(
                cartSummaryService.buildSummary(cart, request.getContextPath())
        ));
    }
}
