package nlu.fit.web.souvenirecommerce.features.cart.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import nlu.fit.web.souvenirecommerce.common.utils.GsonUtil;
import nlu.fit.web.souvenirecommerce.features.cart.model.Cart;
import nlu.fit.web.souvenirecommerce.features.cart.service.CartService;
import nlu.fit.web.souvenirecommerce.features.cart.service.CartSummaryService;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "AddCart", value = "/cart/add")
public class AddCartServlet extends HttpServlet {
    private final CartService cartService = new CartService();
    private final CartSummaryService cartSummaryService = new CartSummaryService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();

        Long productId;
        int quantity;

        try {
            productId = Long.parseLong(request.getParameter("productId"));
            quantity = Integer.parseInt(request.getParameter("quantity"));
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }

        boolean added = cartService.addItem(session, productId, quantity);
        if (!added) {
            if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().print("""
                        {
                          "success": false,
                          "message": "Không thể thêm sản phẩm vào giỏ hàng"
                        }
                        """);
                return;
            }

            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }

        Cart cart = cartService.getCartForDisplay(session);
        cartService.storeCart(session, cart);

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
