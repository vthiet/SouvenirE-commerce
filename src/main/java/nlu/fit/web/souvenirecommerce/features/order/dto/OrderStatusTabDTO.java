package nlu.fit.web.souvenirecommerce.features.order.dto;

import lombok.Getter;

@Getter
public class OrderStatusTabDTO {
    private final String code;
    private final String label;
    private final int count;

    public OrderStatusTabDTO(String code, String label, int count) {
        this.code = code;
        this.label = label;
        this.count = count;
    }

}
