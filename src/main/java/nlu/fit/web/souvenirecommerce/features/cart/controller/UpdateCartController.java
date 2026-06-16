package nlu.fit.web.souvenirecommerce.features.cart.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import nlu.fit.web.souvenirecommerce.features.cart.model.CartEntity;
import nlu.fit.web.souvenirecommerce.features.cart.model.CartItemEntity;
import nlu.fit.web.souvenirecommerce.features.cart.service.CartService;

import java.io.IOException;

@WebServlet("/cart/update")
public class UpdateCartController extends HttpServlet {
    private final CartService cartService = new CartService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            Long productId = Long.parseLong(request.getParameter("productId"));
            int quantity   = Integer.parseInt(request.getParameter("quantity"));

            HttpSession session = request.getSession();
            boolean updated = cartService.updateItem(session, productId, quantity);
            if (!updated) {
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

            response.getWriter().write(json);

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("{\"success\":false}");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }
}
