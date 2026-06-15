package nlu.fit.web.souvenirecommerce.features.payment;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@WebServlet("/payment/vnpay-ipn")
public class VnPayIpnServlet extends HttpServlet {
    private final VnPayService vnPayService = new VnPayService();
    private final PaymentProcessingService paymentService = new PaymentProcessingService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        Map<String, String> fields = VnPayUtil.getRequestParams(request);

        if (!vnPayService.isConfigured()
                || !VnPayUtil.verifySignature(fields, vnPayService.getHashSecret())) {
            writeResponse(response, "97", "Invalid signature");
            return;
        }

        PaymentCallbackResult result = paymentService.processVnPayCallback(fields);
        writeResponse(response, result.getIpnResponseCode(), messageFor(result.getOutcome()));
    }

    private String messageFor(PaymentCallbackResult.Outcome outcome) {
        return switch (outcome) {
            case PROCESSED -> "Confirm Success";
            case ALREADY_PROCESSED -> "Order already confirmed";
            case ORDER_NOT_FOUND -> "Order not found";
            case INVALID_AMOUNT -> "Invalid amount";
            case INVALID_REQUEST -> "Invalid request";
        };
    }

    private void writeResponse(HttpServletResponse response, String code, String message) throws IOException {
        response.getWriter().write("{\"RspCode\":\"" + code + "\",\"Message\":\"" + message + "\"}");
    }
}
