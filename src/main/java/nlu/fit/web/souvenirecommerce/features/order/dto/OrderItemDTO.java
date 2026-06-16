package nlu.fit.web.souvenirecommerce.features.order.dto;

public class OrderItemDTO {
    private long productId;
    private String productName;
    private String productImage;
    private int quantity;
    private double priceAtPurchase;

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

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

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPriceAtPurchase() {
        return priceAtPurchase;
    }

    public void setPriceAtPurchase(double priceAtPurchase) {
        this.priceAtPurchase = priceAtPurchase;
    }

    public double getSubTotal() {
        return priceAtPurchase * quantity;
    }
}
