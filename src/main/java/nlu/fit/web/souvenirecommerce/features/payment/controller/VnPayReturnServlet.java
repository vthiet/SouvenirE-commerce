package nlu.fit.web.souvenirecommerce.features.payment.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nlu.fit.web.souvenirecommerce.common.utils.I18nUtil;
import nlu.fit.web.souvenirecommerce.features.payment.model.PaymentCallbackResult;
import nlu.fit.web.souvenirecommerce.features.payment.model.VnPayUtil;
import nlu.fit.web.souvenirecommerce.features.payment.service.PaymentProcessingService;
import nlu.fit.web.souvenirecommerce.features.payment.service.VnPayService;
import nlu.fit.web.souvenirecommerce.model.enums.PaymentProvider;

import java.io.IOException;
import java.util.Map;

@WebServlet("/payment/vnpay-return")
public class VnPayReturnServlet extends HttpServlet {
    private final VnPayService vnPayService = new VnPayService();
    private final PaymentProcessingService paymentService = new PaymentProcessingService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Map<String, String> fields = VnPayUtil.getRequestParams(request);

        PaymentCallbackResult result = paymentService.processWebhook(PaymentProvider.VNPAY, fields);

        if (result.getOutcome() == PaymentCallbackResult.Outcome.INVALID_REQUEST) {
            request.setAttribute("paymentStatus", "INVALID");
            request.setAttribute("paymentMessage", I18nUtil.message(request, "payment.server.invalid_signature"));
            forwardResult(request, response);
            return;
        }

        boolean isSuccess = result.isSuccessful();
        request.setAttribute("paymentStatus", isSuccess ? "SUCCESS" : "FAILED");
        request.setAttribute("paymentMessage", messageFor(request, result));

        nlu.fit.web.souvenirecommerce.model.entity.PaymentTransaction transaction = result.getTransaction();
        request.setAttribute("paymentTransaction", transaction);
        if (transaction != null) {
            // Because order is no longer directly linked via @OneToOne (or at least we only
            // have orderId),
            // We fetch the order from repo.
            nlu.fit.web.souvenirecommerce.model.entity.Order order = new nlu.fit.web.souvenirecommerce.features.order.repository.OrderRepository()
                    .findById(transaction.getOrderId()).orElse(null);
            request.setAttribute("order", order);
        }
        forwardResult(request, response);
    }

    private String messageFor(HttpServletRequest request, PaymentCallbackResult result) {
        return switch (result.getOutcome()) {
            case PROCESSED -> result.isSuccessful()
                    ? I18nUtil.message(request, "payment.server.processed.success")
                    : I18nUtil.message(request, "payment.server.processed.failed");
            case ALREADY_PROCESSED -> I18nUtil.message(request, "payment.server.already_processed");
            case ORDER_NOT_FOUND -> I18nUtil.message(request, "payment.server.order_not_found");
            case INVALID_AMOUNT -> I18nUtil.message(request, "payment.server.invalid_amount");
            case INVALID_REQUEST -> I18nUtil.message(request, "payment.server.invalid_request");
        };
    }

    private void forwardResult(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/payment/result.jsp").forward(request, response);
    }
}
