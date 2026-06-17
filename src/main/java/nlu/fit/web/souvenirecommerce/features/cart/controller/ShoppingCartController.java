package nlu.fit.web.souvenirecommerce.features.cart.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import nlu.fit.web.souvenirecommerce.common.utils.I18nUtil;
import nlu.fit.web.souvenirecommerce.features.cart.model.CartEntity;
import nlu.fit.web.souvenirecommerce.features.cart.model.CartItemEntity;
import nlu.fit.web.souvenirecommerce.features.cart.service.CartService;

import java.io.IOException;

@WebServlet(name = "ShoppingCartController", value = "/cart")
public class ShoppingCartController extends HttpServlet {
    private final CartService cartService = new CartService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(true);
        CartEntity cart = cartService.getCartForDisplay(session);
        cartService.storeCart(session, cart);

        request.setAttribute("cart", cart);
        request.setAttribute("headerMode", "CHECKOUT_FLOW");
        request.setAttribute("checkoutStep", "CART");
        request.setAttribute("pageTitleKey", "cart.page.title");
        request.setAttribute("pageTitle", I18nUtil.message(request, "cart.page.title"));
        request.setAttribute("pageCss", "cart.css");
        request.setAttribute("pageJs", "ShoppingCart.js");
        request.setAttribute("contentPage", "/cart.jsp");
        request.getRequestDispatcher("/WEB-INF/layout/base.jsp").forward(request, response);
    }
}
