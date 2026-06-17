package nlu.fit.web.souvenirecommerce.features.dashboard.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PurchaseOrderItemSummaryDTO {
    private Long id;
    private Long purchaseOrderId;
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitCost;
    private BigDecimal lineAmount;
    private Integer stockBefore;
    private Integer stockAfter;
}
