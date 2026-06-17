package nlu.fit.web.souvenirecommerce.features.order.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OrderItemDTO {
    private long productId;
    private String productName;
    private String productImage;
    private int quantity;
    private double priceAtPurchase;

    public String getProductImagePath() {
        if (productImage == null || productImage.isBlank()) {
            return "/assets/images/logo.png";
        }
        String image = productImage.trim();
        if (image.startsWith("http://") || image.startsWith("https://") || image.startsWith("/")) {
            return image;
        }
        return "/" + image;
    }

    public double getSubTotal() {
        return priceAtPurchase * quantity;
    }
}
