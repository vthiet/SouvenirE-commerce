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
public class CartEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder.Default
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItemEntity> items = new ArrayList<>();

    // ── Mutation helpers ─────────────────────────────────────────────────────

    public void addItem(CartItemEntity item) {
        item.setCart(this);
        items.add(item);
    }

    public void removeItem(CartItemEntity item) {
        items.remove(item);
        item.setCart(null);
    }

    public void clearItems() {
        items.clear();
    }

    public void mergeItems(List<CartItemEntity> newItems) {
        newItems.forEach(this::addItem);
    }

    // ── Lookup helpers ────────────────────────────────────────────────────────

    /**
     * Finds an item by product ID, or returns {@code null} if not found.
     */
    public CartItemEntity getItem(Long productId) {
        if (productId == null) return null;
        return items.stream()
                .filter(item -> item.getProduct() != null && productId.equals(item.getProduct().getId()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Removes item by product ID. Returns the removed item, or {@code null} if not found.
     */
    public CartItemEntity removeItemByProductId(Long productId) {
        CartItemEntity item = getItem(productId);
        if (item != null) removeItem(item);
        return item;
    }

    /**
     * Updates quantity of an item by product ID. Returns {@code true} if the item was found.
     */
    public boolean updateItemQuantity(Long productId, int quantity) {
        CartItemEntity item = getItem(productId);
        if (item == null) return false;
        item.setQuantity(quantity);
        return true;
    }

    // ── Aggregate helpers ─────────────────────────────────────────────────────

    public int totalQuantity() {
        return items.stream().mapToInt(CartItemEntity::getQuantity).sum();
    }

    public double total() {
        return items.stream().mapToDouble(CartItemEntity::getSubTotal).sum();
    }
}
