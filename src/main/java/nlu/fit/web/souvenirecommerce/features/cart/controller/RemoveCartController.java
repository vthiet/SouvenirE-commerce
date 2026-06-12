package nlu.fit.web.souvenirecommerce.features.cart.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import nlu.fit.web.souvenirecommerce.features.cart.service.NewCartService;

import java.io.IOException;

@WebServlet("/cart/remove")
public class RemoveCartController extends HttpServlet {
    private final NewCartService cartService = new NewCartService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(true);
        try {
            cartService.updateItem(session, Long.parseLong(request.getParameter("productId")), 0);
        } catch (NumberFormatException ignored) {
            // Keep the cart unchanged for invalid product ids.
        }
        response.sendRedirect(request.getContextPath() + "/cart");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/cart");
    }
}
