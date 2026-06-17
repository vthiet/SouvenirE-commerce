package nlu.fit.web.souvenirecommerce.features.payment.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import nlu.fit.web.souvenirecommerce.core.logging.AuditLogService;
import nlu.fit.web.souvenirecommerce.common.utils.I18nUtil;
import nlu.fit.web.souvenirecommerce.features.cart.service.CartService;
import nlu.fit.web.souvenirecommerce.features.payment.model.VnPayUtil;
import nlu.fit.web.souvenirecommerce.features.payment.service.PaymentProcessingService;
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
            AuditLogService.failure(
                    VnPayCreatePaymentServlet.class,
                    "Guest",
                    "PAYMENT",
                    "VNPAY_RETRY_CREATED",
                    "ORDER",
                    AuditLogService.describe("reason", "not_authenticated")
            );
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        try {
            Long orderId = Long.valueOf(request.getParameter("orderId"));
            String clientIp = VnPayUtil.getClientIp(request);
            String returnUrl = buildReturnUrl(request);
            String paymentUrl = paymentService.createRetryUrl(
                    orderId,
                    user.getId(),
                    clientIp,
                    returnUrl);
            AuditLogService.success(
                    VnPayCreatePaymentServlet.class,
                    user,
                    "PAYMENT",
                    "VNPAY_RETRY_CREATED",
                    "ORDER",
                    AuditLogService.describe("orderId", orderId, "clientIp", clientIp, "returnUrl", returnUrl)
            );
            response.sendRedirect(paymentUrl);
        } catch (Exception e) {
            AuditLogService.failure(
                    VnPayCreatePaymentServlet.class,
                    user,
                    "PAYMENT",
                    "VNPAY_RETRY_CREATED",
                    "ORDER",
                    AuditLogService.describe("orderId", request.getParameter("orderId"), "reason", e.getClass().getSimpleName(), "message", e.getMessage())
            );
            request.setAttribute("paymentStatus", "FAILED");
            request.setAttribute("paymentMessage", I18nUtil.message(request, "payment.server.create_failed"));
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
