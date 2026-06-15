package nlu.fit.web.souvenirecommerce.features.cart.model;

import jakarta.persistence.*;
import lombok.*;
import nlu.fit.web.souvenirecommerce.model.entity.Product;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

@Entity
@Table(name = "cart_items")
public class NewCartItem implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cart_id", nullable = false)
    private NewCart cart;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    public void increaseQuantity() {
        this.quantity++;
    }

    public void increaseQuantity(int quantity) {
        this.quantity += quantity;
    }

    public void decreaseQuantity() {
        this.quantity--;
    }

    public void decreaseQuantity(int quantity) {
        this.quantity -= quantity;
    }

    public double getTotalPrice() {
        return getUnitPrice() * quantity;
    }

    public double getSubTotal() {
        return getTotalPrice();
    }

    public double getUnitPrice() {
        if (product.getSalePrice() != null && product.getSalePrice() > 0) {
            return product.getSalePrice();
        }
        return product.getOriginalPrice();
    }
}
