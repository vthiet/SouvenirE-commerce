package nlu.fit.web.souvenirecommerce.features.cart.model;

import jakarta.persistence.*;
import lombok.*;
import nlu.fit.web.souvenirecommerce.model.entity.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

@Entity
@Table(name = "carts")
public class NewCart implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder.Default
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NewCartItem> items = new ArrayList<>();

    public void addItem(NewCartItem item) {
        item.setCart(this);
        items.add(item);
    }

    public void removeItem(NewCartItem item) {
        items.remove(item);
        item.setCart(null);
    }

    public void clearItems() {
        items.clear();
    }

    public void mergeItems(List<NewCartItem> newItems) {
        items.addAll(newItems);
    }

    public int totalQuantity() {
        return items.stream().mapToInt(NewCartItem::getQuantity).sum();
    }

    public double total() {
        return items.stream().mapToDouble(NewCartItem::getSubTotal).sum();
    }
}
