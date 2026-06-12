package nlu.fit.web.souvenirecommerce.features.cart.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import nlu.fit.web.souvenirecommerce.features.cart.model.NewCart;
import nlu.fit.web.souvenirecommerce.features.cart.service.NewCartService;

import java.io.IOException;

@WebServlet(name = "ShoppingCartController", value = "/cart")
public class ShoppingCartController extends HttpServlet {
    private final NewCartService cartService = new NewCartService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(true);
        NewCart cart = cartService.refreshCart(session);

        request.setAttribute("cart", cart);
        request.setAttribute("headerMode", "MENU_BAR");
        request.setAttribute("pageTitle", "Giỏ hàng của bạn - INOLA");
        request.setAttribute("pageCss", "cart.css");
        request.setAttribute("pageJs", "ShoppingCart.js");
        request.setAttribute("contentPage", "/WEB-INF/views/cart/cart.jsp");
        request.getRequestDispatcher("/WEB-INF/layout/base.jsp").forward(request, response);
    }
}
