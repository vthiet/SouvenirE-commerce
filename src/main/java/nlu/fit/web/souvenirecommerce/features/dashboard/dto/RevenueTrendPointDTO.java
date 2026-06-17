package nlu.fit.web.souvenirecommerce.features.dashboard.dto;

import java.math.BigDecimal;

public class RevenueTrendPointDTO {

    private String label;
    private BigDecimal revenue;
    private int orderCount;

    public RevenueTrendPointDTO() {
    }

    public RevenueTrendPointDTO(String label, BigDecimal revenue, int orderCount) {
        this.label = label;
        this.revenue = revenue;
        this.orderCount = orderCount;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }

    public int getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(int orderCount) {
        this.orderCount = orderCount;
    }
}
