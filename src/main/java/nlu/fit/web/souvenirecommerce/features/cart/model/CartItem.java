package nlu.fit.web.souvenirecommerce.features.cart.model;

import lombok.*;
import nlu.fit.web.souvenirecommerce.model.entity.Product;

import java.io.Serializable;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem implements Serializable {
    private Product product;
    private double price;
    private int quantity;

    public void upQuantity(int quantity) {
        this.quantity += quantity;
    }

    public double getSubTotal() {
        return price * quantity;
    }
}
