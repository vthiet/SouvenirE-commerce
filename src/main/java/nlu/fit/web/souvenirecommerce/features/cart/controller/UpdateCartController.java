package nlu.fit.web.souvenirecommerce.features.cart.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import nlu.fit.web.souvenirecommerce.features.cart.model.NewCart;
import nlu.fit.web.souvenirecommerce.features.cart.model.NewCartItem;
import nlu.fit.web.souvenirecommerce.features.cart.service.NewCartService;

import java.io.IOException;

@WebServlet("/cart/update")
public class UpdateCartController extends HttpServlet {
    private final NewCartService cartService = new NewCartService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(true);
        boolean isAjax = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));

        try {
            Long productId = Long.parseLong(request.getParameter("productId"));
            int quantity = Integer.parseInt(request.getParameter("quantity"));
            boolean success = cartService.updateItem(session, productId, quantity);
            NewCart cart = cartService.getCart(session);

            if (!isAjax) {
                response.sendRedirect(request.getContextPath() + "/cart");
                return;
            }

            NewCartItem item = cart.findItem(productId).orElse(null);
            writeJson(response, success, cart.totalQuantity(), cart.total(),
                    item == null ? 0 : item.getSubTotal(),
                    success ? "" : "Số lượng vượt quá tồn kho");
        } catch (NumberFormatException e) {
            if (isAjax) {
                writeJson(response, false, 0, 0, 0, "Dữ liệu cập nhật không hợp lệ");
            } else {
                response.sendRedirect(request.getContextPath() + "/cart");
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/cart");
    }

    private void writeJson(HttpServletResponse response, boolean success, int totalQuantity,
                           double total, double itemSubtotal, String message) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("""
                {"success":%b,"totalQuantity":%d,"total":%.0f,"itemSubtotal":%.0f,"message":"%s"}
                """.formatted(success, totalQuantity, total, itemSubtotal, message));
    }
}
