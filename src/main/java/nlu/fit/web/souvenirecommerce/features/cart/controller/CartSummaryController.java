package nlu.fit.web.souvenirecommerce.features.cart.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import nlu.fit.web.souvenirecommerce.common.utils.GsonUtil;
import nlu.fit.web.souvenirecommerce.features.cart.model.NewCart;
import nlu.fit.web.souvenirecommerce.features.cart.service.NewCartService;

import java.io.IOException;

@WebServlet("/cart/summary")
public class CartSummaryController extends HttpServlet {
    private final NewCartService cartService = new NewCartService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        NewCart cart = session == null ? null : cartService.refreshCart(session);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(GsonUtil.getGson().toJson(
                cartService.buildSummary(cart, request.getContextPath())));
    }
}
