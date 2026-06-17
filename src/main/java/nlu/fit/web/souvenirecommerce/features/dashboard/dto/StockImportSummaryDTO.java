package nlu.fit.web.souvenirecommerce.features.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockImportSummaryDTO {
    private long importCount;
    private long totalQuantity;
    private BigDecimal totalCost;
    private long lowStockCount;
    private Date latestImportAt;
}
