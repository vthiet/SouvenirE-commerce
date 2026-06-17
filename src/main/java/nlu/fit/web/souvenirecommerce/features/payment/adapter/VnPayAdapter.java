package nlu.fit.web.souvenirecommerce.features.payment.adapter;

import nlu.fit.web.souvenirecommerce.features.payment.VnPayService;
import nlu.fit.web.souvenirecommerce.features.payment.VnPayUtil;
import nlu.fit.web.souvenirecommerce.features.payment.port.PaymentProviderAdapter;

import java.math.BigDecimal;
import java.util.Map;

public class VnPayAdapter implements PaymentProviderAdapter {
    
    private final VnPayService vnPayService;

    public VnPayAdapter() {
        this.vnPayService = new VnPayService();
    }

    @Override
    public String createPaymentUrl(Long transactionId, Long orderId, BigDecimal amount, String clientIp, String returnUrl) {
        // VNPay amount is in VND, and the service multiplies it by 100 inside, but we pass transactionId instead of orderId for vnp_TxnRef
        return vnPayService.createPaymentUrl(transactionId, amount.longValue(), clientIp, returnUrl);
    }

    @Override
    public boolean verifySignature(Map<String, String> params) {
        return VnPayUtil.verifySignature(params, vnPayService.getHashSecret());
    }

    @Override
    public boolean isPaymentSuccess(Map<String, String> params) {
        return "00".equals(params.get("vnp_ResponseCode")) && "00".equals(params.get("vnp_TransactionStatus"));
    }

    @Override
    public Long getTransactionId(Map<String, String> params) {
        String txnRef = params.get("vnp_TxnRef");
        if (txnRef != null && !txnRef.isBlank()) {
            try {
                return Long.parseLong(txnRef);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public String getProviderTransactionRef(Map<String, String> params) {
        return params.get("vnp_TransactionNo");
    }

    @Override
    public String getResponseCode(Map<String, String> params) {
        return params.get("vnp_ResponseCode");
    }

    @Override
    public String getBankCode(Map<String, String> params) {
        return params.get("vnp_BankCode");
    }

    @Override
    public BigDecimal getAmount(Map<String, String> params) {
        String amountStr = params.get("vnp_Amount");
        if (amountStr != null && !amountStr.isBlank()) {
            try {
                return new BigDecimal(amountStr).divide(new BigDecimal(100));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
