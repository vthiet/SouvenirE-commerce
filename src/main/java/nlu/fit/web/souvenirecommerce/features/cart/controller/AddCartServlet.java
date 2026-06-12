package nlu.fit.web.souvenirecommerce.features.cart.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import nlu.fit.web.souvenirecommerce.common.utils.GsonUtil;
import nlu.fit.web.souvenirecommerce.features.cart.model.NewCart;
import nlu.fit.web.souvenirecommerce.features.cart.service.NewCartService;

import java.io.IOException;

@WebServlet(name = "AddCart", value = "/cart/add")
public class AddCartServlet extends HttpServlet {
    private final NewCartService cartService = new NewCartService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(true);
        boolean isAjax = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));

        try {
            Long productId = Long.parseLong(request.getParameter("productId"));
            int quantity = Integer.parseInt(request.getParameter("quantity"));
            if (!cartService.addItem(session, productId, quantity)) {
                writeFailure(request, response, isAjax, "Sản phẩm không đủ tồn kho");
                return;
            }

            NewCart cart = cartService.getCart(session);
            if ("true".equals(request.getParameter("buyNow"))) {
                response.sendRedirect(request.getContextPath() + "/checkout");
            } else if (isAjax) {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().print(GsonUtil.getGson().toJson(
                        cartService.buildSummary(cart, request.getContextPath())));
            } else {
                response.sendRedirect(request.getHeader("Referer"));
            }
        } catch (NumberFormatException e) {
            writeFailure(request, response, isAjax, "Dữ liệu sản phẩm không hợp lệ");
        }
    }

    private void writeFailure(HttpServletRequest request, HttpServletResponse response,
                              boolean isAjax, String message) throws IOException {
        if (isAjax) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().print("{\"success\":false,\"message\":\"" + message + "\"}");
        } else {
            response.sendRedirect(request.getContextPath() + "/home");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/home");
    }
}
