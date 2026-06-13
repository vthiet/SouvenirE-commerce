package nlu.fit.web.souvenirecommerce.features.cart.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import nlu.fit.web.souvenirecommerce.common.utils.GsonUtil;
import nlu.fit.web.souvenirecommerce.features.cart.model.Cart;
import nlu.fit.web.souvenirecommerce.features.cart.service.CartPriceService;
import nlu.fit.web.souvenirecommerce.features.cart.service.CartPersistenceService;
import nlu.fit.web.souvenirecommerce.features.cart.service.CartSummaryService;
import nlu.fit.web.souvenirecommerce.legacy.dao.ProductDAO;
import nlu.fit.web.souvenirecommerce.model.entity.Product;
import nlu.fit.web.souvenirecommerce.model.entity.User;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "AddCart", value = "/cart/add")
public class AddCartServlet extends HttpServlet {
    private final CartPersistenceService cartPersistenceService = new CartPersistenceService();
    private final CartSummaryService cartSummaryService = new CartSummaryService();
    private final CartPriceService cartPriceService = new CartPriceService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("userInSession");

        if (user == null) {
            if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().print("""
                        {
                          "success": false,
                          "requireLogin": true,
                          "message": "Vui lòng đăng nhập"
                        }
                        """);
                return;
            }

            session.setAttribute("redirectAfterLogin", request.getHeader("Referer"));
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Long productId;
        int quantity;

        try {
            productId = Long.parseLong(request.getParameter("productId"));
            quantity = Integer.parseInt(request.getParameter("quantity"));
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }

        Product product = new ProductDAO().getProductById(productId);
        if (product == null || quantity <= 0 || quantity > product.getStockQuantity()) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }

        Cart cart = (Cart) session.getAttribute("cart");
        if (cart == null) cart = new Cart();

        cart.addItem(product, quantity, cartPriceService.getCurrentPrice(product));
        session.setAttribute("cart", cart);
        session.setAttribute("cartItemCount", cart.totalQuantity());
        cartPersistenceService.saveCart(user, cart);

        if ("true".equals(request.getParameter("buyNow"))) {
            response.sendRedirect(request.getContextPath() + "/checkout");
            return;
        }

        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            PrintWriter out = response.getWriter();
            out.print(GsonUtil.getGson().toJson(cartSummaryService.buildSummary(cart, request.getContextPath())));
            return;
        }
 
        response.sendRedirect(request.getHeader("Referer"));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.sendRedirect(request.getContextPath() + "/home");
    }
}
