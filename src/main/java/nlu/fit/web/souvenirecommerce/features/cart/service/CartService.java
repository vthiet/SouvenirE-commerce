package nlu.fit.web.souvenirecommerce.features.cart.service;

import jakarta.servlet.http.HttpSession;
import nlu.fit.web.souvenirecommerce.features.cart.model.CartEntity;
import nlu.fit.web.souvenirecommerce.features.cart.model.CartItemEntity;
import nlu.fit.web.souvenirecommerce.features.cart.repository.CartEntityRepository;
import nlu.fit.web.souvenirecommerce.features.cart.repository.CartRepository;
import nlu.fit.web.souvenirecommerce.model.entity.Product;
import nlu.fit.web.souvenirecommerce.model.entity.User;

import java.util.Optional;

/**
 * Central cart service. Manages both session-based carts (guest users) and
 * database-backed carts (authenticated users), using {@link CartEntity} and
 * {@link CartItemEntity} as the single cart model throughout.
 *
 * <p>Guest cart flow:
 * <ol>
 *   <li>An in-memory {@link CartEntity} (no ID, no user) is stored in the HTTP session.</li>
 *   <li>When the guest logs in, the session cart is merged into their database cart.</li>
 * </ol>
 *
 * <p>Authenticated user cart flow:
 * <ol>
 *   <li>Cart is loaded from (or created in) the database via {@link CartEntityRepository}.</li>
 *   <li>A clean, proxy-free copy is cached in the session for display.</li>
 * </ol>
 */
public class CartService {

    private static final String CART_SESSION_KEY = "cart";
    private static final String CART_ITEM_COUNT_SESSION_KEY = "cartItemCount";
    private static final String CART_PENDING_MERGE_SESSION_KEY = "cartPendingMerge";

    private final CartRepository productRepository;          // finds Products
    private final CartEntityRepository cartEntityRepository; // finds/saves CartEntity in DB
    private final CartPriceService cartPriceService = new CartPriceService();

    // ── Constructors ──────────────────────────────────────────────────────────

    public CartService() {
        this(new CartRepository(), new CartEntityRepository());
    }

    public CartService(CartRepository productRepository, CartEntityRepository cartEntityRepository) {
        this.productRepository = productRepository;
        this.cartEntityRepository = cartEntityRepository;
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Returns the cart ready for display.
     * For authenticated users the database cart is used (and a clean session copy is stored).
     * For guests the session cart is used.
     */
    public CartEntity getCartForDisplay(HttpSession session) {
        User user = getCurrentUser(session);
        if (user != null) {
            mergePendingSessionCartToDatabase(user, session);
            CartEntity cart = toSessionCart(getOrCreateDatabaseCart(user.getId()));
            storeCart(session, cart);
            return cart;
        }
        return getOrCreateCart(session);
    }

    /**
     * Returns the current guest session cart, creating an empty one if none exists.
     */
    public CartEntity getOrCreateCart(HttpSession session) {
        Object raw = session.getAttribute(CART_SESSION_KEY);
        if (raw instanceof CartEntity existing) return existing;
        CartEntity fresh = new CartEntity();
        storeCart(session, fresh);
        return fresh;
    }

    /**
     * Returns the current session cart wrapped in an Optional, or empty if not present.
     */
    public Optional<CartEntity> getCart(HttpSession session) {
        if (session == null) return Optional.empty();
        Object raw = session.getAttribute(CART_SESSION_KEY);
        return raw instanceof CartEntity cart ? Optional.of(cart) : Optional.empty();
    }

    /**
     * Adds {@code quantity} units of {@code productId} to the cart.
     * Delegates to the database cart for authenticated users.
     */
    public boolean addItem(HttpSession session, Long productId, int quantity) {
        User user = getCurrentUser(session);
        if (user != null) {
            boolean ok = addDatabaseItem(user.getId(), productId, quantity);
            syncDatabaseCartCount(session, user.getId());
            return ok;
        }
        CartEntity cart = getOrCreateCart(session);
        boolean ok = addItemToCart(cart, productId, quantity);
        storeCart(session, cart);
        return ok;
    }

    /**
     * Adds an item directly to a {@link CartEntity} (used for in-memory / guest carts).
     */
    public boolean addItemToCart(CartEntity cart, Long productId, int quantity) {
        if (cart == null || productId == null || quantity <= 0) return false;
        Optional<Product> product = productRepository.findAvailableProductById(productId);
        if (product.isEmpty()) return false;

        CartItemEntity existing = cart.getItem(productId);
        int current = existing == null ? 0 : existing.getQuantity();
        if (product.get().getStockQuantity() < current + quantity) return false;

        if (existing != null) {
            existing.increaseQuantity(quantity);
        } else {
            cart.addItem(CartItemEntity.builder()
                    .product(product.get())
                    .quantity(quantity)
                    .build());
        }
        return true;
    }

    /**
     * Updates the quantity of {@code productId} in the cart.
     * A quantity ≤ 0 removes the item.
     */
    public boolean updateItem(HttpSession session, Long productId, int quantity) {
        User user = getCurrentUser(session);
        if (user != null) {
            boolean ok = updateDatabaseItem(user.getId(), productId, quantity);
            syncDatabaseCartCount(session, user.getId());
            return ok;
        }
        CartEntity cart = (CartEntity) session.getAttribute(CART_SESSION_KEY);
        if (cart == null) return false;
        boolean ok = updateItemInCart(cart, productId, quantity);
        storeCart(session, cart);
        return ok;
    }

    /**
     * Updates an item in a {@link CartEntity} (in-memory / guest carts).
     */
    public boolean updateItemInCart(CartEntity cart, Long productId, int quantity) {
        if (cart == null || productId == null) return false;
        if (quantity <= 0) return cart.removeItemByProductId(productId) != null;
        Optional<Product> product = findProductIfValid(productId, quantity);
        if (product.isEmpty()) return false;
        return cart.updateItemQuantity(productId, quantity);
    }

    /**
     * Removes {@code productId} from the cart.
     */
    public boolean removeItem(HttpSession session, Long productId) {
        User user = getCurrentUser(session);
        if (user != null) {
            boolean ok = removeDatabaseItem(user.getId(), productId);
            syncDatabaseCartCount(session, user.getId());
            return ok;
        }
        CartEntity cart = (CartEntity) session.getAttribute(CART_SESSION_KEY);
        if (cart == null) return false;
        boolean ok = cart.removeItemByProductId(productId) != null;
        storeCart(session, cart);
        return ok;
    }

    /**
     * Stores {@code cart} in the session and keeps the item-count counter in sync.
     */
    public void storeCart(HttpSession session, CartEntity cart) {
        session.setAttribute(CART_SESSION_KEY, cart);
        session.setAttribute(CART_ITEM_COUNT_SESSION_KEY, cart.totalQuantity());
    }

    /**
     * Marks the guest session cart as "pending merge" so it will be merged into the
     * database cart after the user logs in.
     */
    public void prepareGuestCartMerge(HttpSession session, CartEntity cart) {
        if (session == null || cart == null || cart.totalQuantity() <= 0) return;
        session.setAttribute(CART_SESSION_KEY, cart);
        session.setAttribute(CART_PENDING_MERGE_SESSION_KEY, true);
        session.setAttribute(CART_ITEM_COUNT_SESSION_KEY, cart.totalQuantity());
    }

    /**
     * Returns the total item count across the cart (DB cart for users, session cart for guests).
     */
    public int totalQuantity(HttpSession session) {
        User user = getCurrentUser(session);
        if (user != null) {
            mergePendingSessionCartToDatabase(user, session);
            return cartEntityRepository.findByUserId(user.getId())
                    .map(CartEntity::totalQuantity)
                    .orElse(0);
        }
        return getCart(session).map(CartEntity::totalQuantity).orElse(0);
    }

    /**
     * Clears all items from the database cart of {@code userId}.
     */
    public void clearUserCart(Long userId) {
        if (userId == null) return;
        cartEntityRepository.findByUserId(userId).ifPresent(CartEntity::clearItems);
    }

    /**
     * Removes the session cart and resets the item-count counter.
     */
    public void clearSessionCart(HttpSession session) {
        if (session == null) return;
        session.removeAttribute(CART_SESSION_KEY);
        session.setAttribute(CART_ITEM_COUNT_SESSION_KEY, 0);
    }

    /**
     * Resolves the current authenticated user from the session, trying multiple attribute keys
     * used by different parts of the application.
     */
    public User getCurrentUser(HttpSession session) {
        if (session == null) return null;
        for (String key : new String[]{"userInSession", "currentUser", "userDto", "user", "authUser"}) {
            Object attr = session.getAttribute(key);
            if (attr instanceof User u) return u;
        }
        return null;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private Optional<Product> findProductIfValid(Long productId, int quantity) {
        if (quantity <= 0) return Optional.empty();
        Optional<Product> product = productRepository.findAvailableProductById(productId);
        if (product.isEmpty() || product.get().getStockQuantity() < quantity) return Optional.empty();
        return product;
    }

    private CartEntity getOrCreateDatabaseCart(Long userId) {
        return cartEntityRepository.findByUserId(userId)
                .orElseGet(() -> cartEntityRepository.createForUser(userId));
    }

    /**
     * Creates a clean, non-proxy {@link CartEntity} copy suitable for session storage.
     * Prices are refreshed via {@link CartPriceService} so promotions are reflected.
     */
    private CartEntity toSessionCart(CartEntity dbCart) {
        CartEntity sessionCart = new CartEntity();
        for (CartItemEntity dbItem : dbCart.getItems()) {
            sessionCart.addItem(CartItemEntity.builder()
                    .product(dbItem.getProduct())
                    .quantity(dbItem.getQuantity())
                    .build());
        }
        return sessionCart;
    }

    private boolean addDatabaseItem(Long userId, Long productId, int quantity) {
        if (userId == null || productId == null || quantity <= 0) return false;
        Optional<Product> product = productRepository.findAvailableProductById(productId);
        if (product.isEmpty()) return false;

        CartEntity cart = getOrCreateDatabaseCart(userId);
        CartItemEntity existing = cart.getItem(productId);
        int current = existing == null ? 0 : existing.getQuantity();
        if (product.get().getStockQuantity() < current + quantity) return false;

        if (existing != null) {
            existing.increaseQuantity(quantity);
        } else {
            cart.addItem(CartItemEntity.builder()
                    .product(product.get())
                    .quantity(quantity)
                    .build());
        }
        return true;
    }

    private boolean updateDatabaseItem(Long userId, Long productId, int quantity) {
        if (userId == null || productId == null) return false;
        CartEntity cart = getOrCreateDatabaseCart(userId);
        CartItemEntity item = cart.getItem(productId);
        if (item == null) return false;
        if (quantity <= 0) {
            cart.removeItem(item);
            return true;
        }
        Optional<Product> product = findProductIfValid(productId, quantity);
        if (product.isEmpty()) return false;
        item.setQuantity(quantity);
        return true;
    }

    private boolean removeDatabaseItem(Long userId, Long productId) {
        if (userId == null || productId == null) return false;
        CartEntity cart = getOrCreateDatabaseCart(userId);
        CartItemEntity item = cart.getItem(productId);
        if (item != null) cart.removeItem(item);
        return item != null;
    }

    private void mergePendingSessionCartToDatabase(User user, HttpSession session) {
        if (user == null || session == null) return;
        if (!Boolean.TRUE.equals(session.getAttribute(CART_PENDING_MERGE_SESSION_KEY))) {
            syncDatabaseCartCount(session, user.getId());
            return;
        }
        Object raw = session.getAttribute(CART_SESSION_KEY);
        if (!(raw instanceof CartEntity sessionCart) || sessionCart.totalQuantity() == 0) {
            session.removeAttribute(CART_PENDING_MERGE_SESSION_KEY);
            syncDatabaseCartCount(session, user.getId());
            return;
        }
        for (CartItemEntity item : sessionCart.getItems()) {
            addDatabaseItem(user.getId(), item.getProduct().getId(), item.getQuantity());
        }
        session.removeAttribute(CART_SESSION_KEY);
        session.removeAttribute(CART_PENDING_MERGE_SESSION_KEY);
        syncDatabaseCartCount(session, user.getId());
    }

    private void syncDatabaseCartCount(HttpSession session, Long userId) {
        if (session == null || userId == null) return;
        session.setAttribute(CART_ITEM_COUNT_SESSION_KEY,
                getOrCreateDatabaseCart(userId).totalQuantity());
    }
}
