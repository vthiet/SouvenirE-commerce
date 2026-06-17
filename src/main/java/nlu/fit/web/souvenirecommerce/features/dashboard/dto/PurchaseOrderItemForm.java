package nlu.fit.web.souvenirecommerce.features.dashboard.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PurchaseOrderItemForm {
    private Long productId;
    private Integer quantity;
    private BigDecimal unitCost;
}
