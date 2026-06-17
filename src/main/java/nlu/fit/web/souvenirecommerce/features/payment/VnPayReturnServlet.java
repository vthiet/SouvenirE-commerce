package nlu.fit.web.souvenirecommerce.features.payment;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nlu.fit.web.souvenirecommerce.common.utils.I18nUtil;

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

        if (!vnPayService.isConfigured()
                || !VnPayUtil.verifySignature(fields, vnPayService.getHashSecret())) {
            request.setAttribute("paymentStatus", "INVALID");
            request.setAttribute("paymentMessage", I18nUtil.message(request, "payment.server.invalid_signature"));
            forwardResult(request, response);
            return;
        }

        PaymentCallbackResult result = paymentService.processVnPayCallback(fields);
        request.setAttribute("paymentStatus", result.isSuccessful() ? "SUCCESS" : "FAILED");
        request.setAttribute("paymentMessage", messageFor(request, result));
        request.setAttribute("paymentTransaction", result.getTransaction());
        if (result.getTransaction() != null) {
            request.setAttribute("order", result.getTransaction().getOrder());
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
