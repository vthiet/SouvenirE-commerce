package nlu.fit.web.souvenirecommerce.features.payment;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
            request.setAttribute("paymentMessage", "Không thể xác thực phản hồi từ VNPay.");
            forwardResult(request, response);
            return;
        }

        PaymentCallbackResult result = paymentService.processVnPayCallback(fields);
        request.setAttribute("paymentStatus", result.isSuccessful() ? "SUCCESS" : "FAILED");
        request.setAttribute("paymentMessage", messageFor(result));
        request.setAttribute("paymentTransaction", result.getTransaction());
        if (result.getTransaction() != null) {
            request.setAttribute("order", result.getTransaction().getOrder());
        }
        forwardResult(request, response);
    }

    private String messageFor(PaymentCallbackResult result) {
        return switch (result.getOutcome()) {
            case PROCESSED -> result.isSuccessful()
                    ? "Giao dịch đã được xác nhận thành công."
                    : "Giao dịch chưa hoàn tất hoặc đã bị hủy.";
            case ALREADY_PROCESSED -> "Đơn hàng này đã được thanh toán trước đó.";
            case ORDER_NOT_FOUND -> "Không tìm thấy đơn hàng tương ứng với giao dịch.";
            case INVALID_AMOUNT -> "Số tiền thanh toán không khớp với đơn hàng.";
            case INVALID_REQUEST -> "Phản hồi thanh toán không hợp lệ.";
        };
    }

    private void forwardResult(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/payment/result.jsp").forward(request, response);
    }
}
