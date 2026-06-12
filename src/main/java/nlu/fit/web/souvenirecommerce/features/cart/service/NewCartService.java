package nlu.fit.web.souvenirecommerce.features.cart.service;

import jakarta.servlet.http.HttpSession;
import nlu.fit.web.souvenirecommerce.common.base.AbsBaseService;
import nlu.fit.web.souvenirecommerce.features.cart.model.NewCart;
import nlu.fit.web.souvenirecommerce.features.cart.model.NewCartItem;
import nlu.fit.web.souvenirecommerce.features.cart.repository.NewCartRepository;
import nlu.fit.web.souvenirecommerce.legacy.utils.DBContext;
import nlu.fit.web.souvenirecommerce.model.entity.Product;
import nlu.fit.web.souvenirecommerce.model.entity.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class NewCartService extends AbsBaseService<Long, NewCart> {
    private static final String CART_SESSION_KEY = "cart";
    private static final String CART_ITEM_COUNT_SESSION_KEY = "cartItemCount";

    private final NewCartRepository cartRepository;
    private final NewCartItemService cartItemService;

    public NewCartService() {
        this(new NewCartRepository());
    }

    NewCartService(NewCartRepository cartRepository) {
        super(cartRepository);
        this.cartRepository = cartRepository;
        this.cartItemService = new NewCartItemService(cartRepository);
    }

    public Optional<NewCart> findCartByUserId(Long userId) {
        return cartRepository.findByUserId(userId);
    }

    public NewCart getCart(User user) {
        if (user == null || user.getId() == null) {
            return new NewCart();
        }
        return findCartByUserId(user.getId())
                .orElseGet(() -> cartRepository.createForUser(user.getId()));
    }

    public NewCart getCart(HttpSession session) {
        if (session == null) {
            return new NewCart();
        }
        User user = getCurrentUser(session);
        if (user != null) {
            return getCart(user);
        }
        Object cart = session.getAttribute(CART_SESSION_KEY);
        if (cart instanceof NewCart guestCart) {
            return guestCart;
        }
        NewCart guestCart = new NewCart();
        storeInSession(session, guestCart);
        return guestCart;
    }

    public NewCart refreshCart(HttpSession session) {
        NewCart cart = getCart(session);
        for (NewCartItem item : cart.getItems()) {
            item.setPrice(getCurrentPrice(item.getProduct()));
        }
        storeInSession(session, cart);
        return cart;
    }

    public NewCart refreshCart(User user) {
        NewCart cart = getCart(user);
        cart.getItems().forEach(item -> item.setPrice(getCurrentPrice(item.getProduct())));
        return cart;
    }

    public boolean addItem(HttpSession session, Long productId, int quantity) {
        NewCart cart = getCart(session);
        boolean success = cartItemService.addItem(cart, productId, quantity, this::getCurrentPrice);
        storeInSession(session, cart);
        return success;
    }

    public boolean updateItem(HttpSession session, Long productId, int quantity) {
        NewCart cart = getCart(session);
        boolean success = cartItemService.updateItem(cart, productId, quantity, this::getCurrentPrice);
        storeInSession(session, cart);
        return success;
    }

    public void mergeGuestCart(User user, NewCart guestCart) {
        if (user == null || guestCart == null || guestCart.totalQuantity() == 0) {
            return;
        }
        NewCart userCart = getCart(user);
        for (NewCartItem item : List.copyOf(guestCart.getItems())) {
            cartItemService.addItem(userCart, item.getProduct().getId(), item.getQuantity(), this::getCurrentPrice);
        }
    }

    public void storeInSession(HttpSession session, NewCart cart) {
        if (session == null) {
            return;
        }
        session.setAttribute(CART_SESSION_KEY, cart);
        session.setAttribute(CART_ITEM_COUNT_SESSION_KEY, cart == null ? 0 : cart.totalQuantity());
    }

    public int totalQuantity(HttpSession session) {
        return getCart(session).totalQuantity();
    }

    public Map<String, Object> buildSummary(NewCart cart, String contextPath) {
        NewCart safeCart = cart == null ? new NewCart() : cart;
        Map<String, Object> response = new LinkedHashMap<>();
        List<Map<String, Object>> items = new ArrayList<>();

        for (NewCartItem cartItem : safeCart.getItems()) {
            Product product = cartItem.getProduct();
            if (product == null) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", product.getId());
            item.put("name", product.getName());
            item.put("imageUrl", normalizeImageUrl(product.getImageUrl(), contextPath));
            item.put("price", cartItem.getPrice());
            item.put("quantity", cartItem.getQuantity());
            items.add(item);
        }

        response.put("success", true);
        response.put("cartCount", safeCart.totalQuantity());
        response.put("items", items);
        return response;
    }

    public double getCurrentPrice(Product product) {
        if (product == null || product.getId() == null) {
            return 0;
        }
        Integer discountPercent = getBestActiveDiscountPercent(product.getId());
        if (discountPercent == null || discountPercent <= 0) {
            return product.getOriginalPrice();
        }
        return product.getOriginalPrice() * (100 - discountPercent) / 100.0;
    }

    public User getCurrentUser(HttpSession session) {
        if (session == null) {
            return null;
        }
        for (String key : new String[]{"userInSession", "currentUser", "userDto", "user", "authUser"}) {
            Object value = session.getAttribute(key);
            if (value instanceof User user) {
                return user;
            }
        }
        return null;
    }

    private Integer getBestActiveDiscountPercent(Long productId) {
        String sql = """
                SELECT discount_percent
                FROM promotions
                WHERE product_id = ?
                  AND (start_date IS NULL OR start_date <= CURRENT_TIMESTAMP)
                  AND (end_date IS NULL OR end_date >= CURRENT_TIMESTAMP)
                ORDER BY discount_percent DESC
                LIMIT 1
                """;

        try (Connection connection = DBContext.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, productId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt("discount_percent") : null;
            }
        } catch (Exception e) {
            log.warn("Cannot load current price for productId={}", productId, e);
            return null;
        }
    }

    private String normalizeImageUrl(String imageUrl, String contextPath) {
        if (imageUrl == null || imageUrl.isBlank() || imageUrl.startsWith("http")
                || imageUrl.startsWith("data:")) {
            return imageUrl;
        }
        String prefix = contextPath == null ? "" : contextPath;
        String path = imageUrl.startsWith("/") ? imageUrl : "/" + imageUrl;
        return path.startsWith(prefix + "/") ? path : prefix + path;
    }
}
