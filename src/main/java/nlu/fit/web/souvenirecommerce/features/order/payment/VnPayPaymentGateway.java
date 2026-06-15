package nlu.fit.web.souvenirecommerce.features.order.payment;

import nlu.fit.web.souvenirecommerce.features.order.dto.CheckoutException;
import nlu.fit.web.souvenirecommerce.features.order.dto.PaymentContext;
import nlu.fit.web.souvenirecommerce.features.order.dto.PaymentPreparation;
import nlu.fit.web.souvenirecommerce.features.payment.VnPayService;
import nlu.fit.web.souvenirecommerce.model.entity.Order;
import nlu.fit.web.souvenirecommerce.model.enums.PaymentMethod;
import nlu.fit.web.souvenirecommerce.model.enums.PaymentProvider;
import nlu.fit.web.souvenirecommerce.model.enums.PaymentStatus;

public class VnPayPaymentGateway implements PaymentGateway {
    private final VnPayService vnPayService = new VnPayService();

    @Override
    public PaymentMethod method() {
        return PaymentMethod.VNPAY_QR;
    }

    @Override
    public boolean isAvailable() {
        return vnPayService.isConfigured();
    }

    @Override
    public PaymentPreparation prepare(Order order, PaymentContext context) {
        if (!isAvailable()) {
            throw new CheckoutException("VNPay chưa được cấu hình. Vui lòng chọn COD hoặc liên hệ quản trị viên.");
        }
        if (order.getId() == null) {
            throw new CheckoutException("Không thể tạo giao dịch VNPay cho đơn hàng chưa được lưu.");
        }
        if (context == null) {
            throw new CheckoutException("Không thể tạo phiên thanh toán VNPay. Vui lòng thử lại.");
        }

        String paymentUrl = vnPayService.createPaymentUrl(
                order.getId(),
                order.getTotalAmount().longValueExact(),
                context.getClientIp(),
                context.getReturnUrl());

        return PaymentPreparation.builder()
                .provider(PaymentProvider.VNPAY)
                .status(PaymentStatus.PENDING)
                .paymentUrl(paymentUrl)
                .build();
    }
}
