package nlu.fit.web.souvenirecommerce.features.order.dto;

import lombok.Builder;
import lombok.Getter;
import nlu.fit.web.souvenirecommerce.model.enums.PaymentMethod;

@Getter
@Builder
public class CheckoutRequest {
    private final Long savedAddressId;
    private final String receiverName;
    private final String receiverPhone;
    private final String addressDetail;
    private final Integer provinceCode;
    private final Integer wardCode;
    private final Integer ghnProvinceId;
    private final Integer ghnDistrictId;
    private final String ghnWardCode;
    private final String provinceName;
    private final String districtName;
    private final String wardName;
    private final Double shippingFee;
    private final String note;
    private final PaymentMethod paymentMethod;
}
