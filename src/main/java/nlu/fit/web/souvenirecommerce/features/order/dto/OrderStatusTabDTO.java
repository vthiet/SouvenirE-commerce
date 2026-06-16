package nlu.fit.web.souvenirecommerce.features.order.dto;

public class OrderStatusTabDTO {
    private final String code;
    private final String label;
    private final int count;

    public OrderStatusTabDTO(String code, String label, int count) {
        this.code = code;
        this.label = label;
        this.count = count;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public int getCount() {
        return count;
    }
}
