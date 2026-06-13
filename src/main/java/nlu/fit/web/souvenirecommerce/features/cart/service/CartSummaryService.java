package nlu.fit.web.souvenirecommerce.features.cart.service;

import nlu.fit.web.souvenirecommerce.features.cart.model.Cart;
import nlu.fit.web.souvenirecommerce.features.cart.model.CartItem;
import nlu.fit.web.souvenirecommerce.model.entity.Product;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CartSummaryService {
    private final CartPriceService cartPriceService = new CartPriceService();

    public Map<String, Object> buildSummary(Cart cart, String contextPath) {
        Cart safeCart = cart == null ? new Cart() : cart;
        Map<String, Object> response = new LinkedHashMap<>();
        List<Map<String, Object>> items = new ArrayList<>();

        for (CartItem cartItem : safeCart.getItems()) {
            Product product = cartItem.getProduct();

            if (product == null) {
                continue;
            }

            double currentPrice = cartPriceService.getCurrentPrice(product);
            cartItem.setPrice(currentPrice);

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", product.getId());
            item.put("name", product.getName());
            item.put("imageUrl", normalizeImageUrl(product.getImageUrl(), contextPath));
            item.put("price", currentPrice);
            item.put("quantity", cartItem.getQuantity());
            items.add(item);
        }

        response.put("success", true);
        response.put("cartCount", safeCart.totalQuantity());
        response.put("items", items);

        return response;
    }

    public String normalizeImageUrl(String imageUrl, String contextPath) {
        if (imageUrl == null || imageUrl.isBlank()
                || imageUrl.startsWith("http://")
                || imageUrl.startsWith("https://")
                || imageUrl.startsWith("data:")) {
            return imageUrl;
        }

        String normalizedContextPath = contextPath == null ? "" : contextPath;
        String normalizedImageUrl = imageUrl.startsWith("/") ? imageUrl : "/" + imageUrl;

        if (!normalizedContextPath.isBlank() && normalizedImageUrl.startsWith(normalizedContextPath + "/")) {
            return normalizedImageUrl;
        }

        return normalizedContextPath + normalizedImageUrl;
    }
}
