package nlu.fit.web.souvenirecommerce.features.cart.service;

import nlu.fit.web.souvenirecommerce.features.cart.model.NewCart;
import nlu.fit.web.souvenirecommerce.features.cart.model.NewCartItem;
import nlu.fit.web.souvenirecommerce.features.cart.repository.NewCartRepository;
import nlu.fit.web.souvenirecommerce.model.entity.Product;

import java.util.Optional;
import java.util.function.ToDoubleFunction;

public class NewCartItemService {
    private final NewCartRepository cartRepository;

    public NewCartItemService() {
        this(new NewCartRepository());
    }

    NewCartItemService(NewCartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public boolean addItem(NewCart cart, Long productId, int quantity, ToDoubleFunction<Product> priceProvider) {
        if (cart == null || productId == null || quantity <= 0) {
            return false;
        }

        Optional<Product> productResult = cartRepository.findAvailableProductById(productId);
        if (productResult.isEmpty()) {
            return false;
        }

        Product product = productResult.get();
        NewCartItem item = cart.findItem(productId).orElse(null);
        int currentQuantity = item == null ? 0 : item.getQuantity();
        if (product.getStockQuantity() < currentQuantity + quantity) {
            return false;
        }

        double price = priceProvider.applyAsDouble(product);
        if (item == null) {
            cart.addItem(NewCartItem.builder().product(product).quantity(quantity).price(price).build());
        } else {
            item.increaseQuantity(quantity);
            item.setPrice(price);
        }
        return true;
    }

    public boolean updateItem(NewCart cart, Long productId, int quantity, ToDoubleFunction<Product> priceProvider) {
        if (cart == null || productId == null) {
            return false;
        }

        Optional<NewCartItem> itemResult = cart.findItem(productId);
        if (itemResult.isEmpty()) {
            return false;
        }
        if (quantity <= 0) {
            cart.removeItem(itemResult.get());
            return true;
        }

        Optional<Product> productResult = cartRepository.findAvailableProductById(productId);
        if (productResult.isEmpty() || productResult.get().getStockQuantity() < quantity) {
            return false;
        }

        itemResult.get().setQuantity(quantity);
        itemResult.get().setPrice(priceProvider.applyAsDouble(productResult.get()));
        return true;
    }
}
