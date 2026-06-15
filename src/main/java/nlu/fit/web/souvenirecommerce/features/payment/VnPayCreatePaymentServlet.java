package nlu.fit.web.souvenirecommerce.features.payment;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import nlu.fit.web.souvenirecommerce.features.cart.service.CartService;
import nlu.fit.web.souvenirecommerce.features.order.dto.CheckoutException;
import nlu.fit.web.souvenirecommerce.model.entity.User;

import java.io.IOException;

@WebServlet("/payment/vnpay-create")
public class VnPayCreatePaymentServlet extends HttpServlet {
    private final PaymentProcessingService paymentService = new PaymentProcessingService();
    private final CartService cartService = new CartService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        createPayment(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        createPayment(request, response);
    }

    private void createPayment(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = cartService.getCurrentUser(session);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        try {
            Long orderId = Long.valueOf(request.getParameter("orderId"));
            String paymentUrl = paymentService.createRetryUrl(
                    orderId,
                    user.getId(),
                    VnPayUtil.getClientIp(request),
                    buildReturnUrl(request));
            response.sendRedirect(paymentUrl);
        } catch (NumberFormatException | CheckoutException | IllegalStateException e) {
            request.setAttribute("paymentStatus", "FAILED");
            request.setAttribute("paymentMessage", e.getMessage() == null
                    ? "Không thể tạo lại giao dịch VNPay."
                    : e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/payment/result.jsp").forward(request, response);
        }
    }

    private String buildReturnUrl(HttpServletRequest request) {
        boolean defaultPort = ("http".equalsIgnoreCase(request.getScheme()) && request.getServerPort() == 80)
                || ("https".equalsIgnoreCase(request.getScheme()) && request.getServerPort() == 443);
        return request.getScheme() + "://" + request.getServerName()
                + (defaultPort ? "" : ":" + request.getServerPort())
                + request.getContextPath() + "/payment/vnpay-return";
    }
}
