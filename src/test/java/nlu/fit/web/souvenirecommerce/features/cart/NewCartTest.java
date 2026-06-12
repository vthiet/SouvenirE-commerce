package nlu.fit.web.souvenirecommerce.features.cart;

import nlu.fit.web.souvenirecommerce.features.cart.model.NewCart;
import nlu.fit.web.souvenirecommerce.features.cart.model.NewCartItem;
import nlu.fit.web.souvenirecommerce.model.entity.Product;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NewCartTest {

    @Test
    void calculatesTotalsFromStoredItemPrice() {
        Product product = Product.builder().id(10L).originalPrice(150_000).build();
        NewCart cart = NewCart.builder().build();
        NewCartItem item = NewCartItem.builder()
                .product(product)
                .price(120_000)
                .quantity(2)
                .build();

        cart.addItem(item);

        assertEquals(2, cart.totalQuantity());
        assertEquals(240_000, cart.total());
        assertEquals(120_000, item.getUnitPrice());
        assertTrue(cart.findItem(product.getId()).isPresent());
    }

    @Test
    void removesItemByProductLookup() {
        Product product = Product.builder().id(20L).build();
        NewCart cart = new NewCart();
        NewCartItem item = NewCartItem.builder().product(product).price(50_000).quantity(1).build();
        cart.addItem(item);

        cart.findItem(product.getId()).ifPresent(cart::removeItem);

        assertEquals(0, cart.totalQuantity());
        assertEquals(0, cart.total());
    }
}
