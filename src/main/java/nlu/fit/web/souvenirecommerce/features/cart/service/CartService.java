package nlu.fit.web.souvenirecommerce.features.cart.service;

import jakarta.servlet.http.HttpSession;
import nlu.fit.web.souvenirecommerce.features.cart.model.Cart;
import nlu.fit.web.souvenirecommerce.features.cart.model.CartItem;
import nlu.fit.web.souvenirecommerce.features.cart.model.NewCart;
import nlu.fit.web.souvenirecommerce.features.cart.model.NewCartItem;
import nlu.fit.web.souvenirecommerce.features.cart.repository.CartRepository;
import nlu.fit.web.souvenirecommerce.features.cart.repository.NewCartRepository;
import nlu.fit.web.souvenirecommerce.model.entity.Product;
import nlu.fit.web.souvenirecommerce.model.entity.User;

import java.util.Optional;

public class CartService {
    private static final String CART_SESSION_KEY = "cart";
    private static final String CART_ITEM_COUNT_SESSION_KEY = "cartItemCount";

    private final CartRepository cartRepository;
    private final NewCartRepository newCartRepository;

    public CartService() {
        this(new CartRepository(), new NewCartRepository());
    }

    public CartService(CartRepository cartRepository) {
        this(cartRepository, new NewCartRepository());
    }

    public CartService(CartRepository cartRepository, NewCartRepository newCartRepository) {
        this.cartRepository = cartRepository;
        this.newCartRepository = newCartRepository;
    }

    public Cart getCartForDisplay(HttpSession session) {
        User user = getCurrentUser(session);
        if (user != null) {
            mergeSessionCartToDatabase(user, session);
            return toSessionCart(getOrCreateDatabaseCart(user.getId()));
        }

        return getOrCreateCart(session);
    }

    public Cart getOrCreateCart(HttpSession session) {
        Object cart = session.getAttribute(CART_SESSION_KEY);
        if (cart instanceof Cart existingCart) {
            return existingCart;
        }

        Cart newCart = new Cart();
        storeCart(session, newCart);
        return newCart;
    }

    public Optional<Cart> getCart(HttpSession session) {
        if (session == null) {
            return Optional.empty();
        }

        Object cart = session.getAttribute(CART_SESSION_KEY);
        return cart instanceof Cart existingCart ? Optional.of(existingCart) : Optional.empty();
    }

    public boolean addItem(HttpSession session, Long productId, int quantity) {
        User user = getCurrentUser(session);
        if (user != null) {
            boolean success = addDatabaseItem(user.getId(), productId, quantity);
            syncDatabaseCartCount(session, user.getId());
            return success;
        }

        Cart cart = getOrCreateCart(session);
        boolean success = addItem(cart, productId, quantity);
        storeCart(session, cart);
        return success;
    }

    public boolean addItem(Cart cart, Long productId, int quantity) {
        if (cart == null || productId == null || quantity <= 0) {
            return false;
        }

        Optional<Product> product = cartRepository.findAvailableProductById(productId);
        if (product.isEmpty()) {
            return false;
        }

        int currentQuantity = Optional.ofNullable(cart.getItem(productId))
                .map(CartItem::getQuantity)
                .orElse(0);
        if (product.get().getStockQuantity() < currentQuantity + quantity) {
            return false;
        }

        cart.addItem(product.get(), quantity);
        return true;
    }

    public boolean updateItem(HttpSession session, Long productId, int quantity) {
        User user = getCurrentUser(session);
        if (user != null) {
            boolean success = updateDatabaseItem(user.getId(), productId, quantity);
            syncDatabaseCartCount(session, user.getId());
            return success;
        }

        Cart cart = (Cart) session.getAttribute(CART_SESSION_KEY);
        if (cart == null) {
            return false;
        }

        boolean success = updateItem(cart, productId, quantity);
        storeCart(session, cart);
        return success;
    }

    public boolean updateItem(Cart cart, Long productId, int quantity) {
        if (cart == null || productId == null) {
            return false;
        }

        if (quantity <= 0) {
            return cart.removeItem(productId) != null;
        }

        Optional<Product> product = findProductIfValid(productId, quantity);
        if (product.isEmpty()) {
            return false;
        }

        return cart.updateItem(productId, quantity);
    }

    public boolean removeItem(HttpSession session, Long productId) {
        User user = getCurrentUser(session);
        if (user != null) {
            boolean success = removeDatabaseItem(user.getId(), productId);
            syncDatabaseCartCount(session, user.getId());
            return success;
        }

        Cart cart = (Cart) session.getAttribute(CART_SESSION_KEY);
        if (cart == null) {
            return false;
        }

        boolean success = cart.removeItem(productId) != null;
        storeCart(session, cart);
        return success;
    }

    public void storeCart(HttpSession session, Cart cart) {
        session.setAttribute(CART_SESSION_KEY, cart);
        session.setAttribute(CART_ITEM_COUNT_SESSION_KEY, cart.totalQuantity());
    }

    public int totalQuantity(HttpSession session) {
        User user = getCurrentUser(session);
        if (user != null) {
            Object sessionCart = session.getAttribute(CART_SESSION_KEY);
            if (sessionCart instanceof Cart cart && cart.totalQuantity() > 0) {
                mergeSessionCartToDatabase(user, session);
            }

            return newCartRepository.findByUserId(user.getId())
                    .map(NewCart::totalQuantity)
                    .orElse(0);
        }

        return getCart(session).map(Cart::totalQuantity).orElse(0);
    }

    public void clearUserCart(Long userId) {
        if (userId == null) {
            return;
        }

        newCartRepository.findByUserId(userId).ifPresent(NewCart::clearItems);
    }

    public void clearSessionCart(HttpSession session) {
        if (session == null) {
            return;
        }

        session.removeAttribute(CART_SESSION_KEY);
        session.setAttribute(CART_ITEM_COUNT_SESSION_KEY, 0);
    }

    public User getCurrentUser(HttpSession session) {
        if (session == null) {
            return null;
        }

        Object user = session.getAttribute("userInSession");
        if (user instanceof User currentUser) {
            return currentUser;
        }

        user = session.getAttribute("currentUser");
        if (user instanceof User currentUser) {
            return currentUser;
        }

        user = session.getAttribute("userDto");
        if (user instanceof User currentUser) {
            return currentUser;
        }

        user = session.getAttribute("user");
        if (user instanceof User currentUser) {
            return currentUser;
        }

        user = session.getAttribute("authUser");
        return user instanceof User currentUser ? currentUser : null;
    }

    private Optional<Product> findProductIfValid(Long productId, int quantity) {
        if (quantity <= 0) {
            return Optional.empty();
        }

        Optional<Product> product = cartRepository.findAvailableProductById(productId);
        if (product.isEmpty() || product.get().getStockQuantity() < quantity) {
            return Optional.empty();
        }

        return product;
    }

    private NewCart getOrCreateDatabaseCart(Long userId) {
        return newCartRepository.findByUserId(userId)
                .orElseGet(() -> newCartRepository.createForUser(userId));
    }

    private boolean addDatabaseItem(Long userId, Long productId, int quantity) {
        if (userId == null || productId == null || quantity <= 0) {
            return false;
        }

        Optional<Product> product = cartRepository.findAvailableProductById(productId);
        if (product.isEmpty()) {
            return false;
        }

        NewCart cart = getOrCreateDatabaseCart(userId);
        NewCartItem item = findDatabaseItem(cart, productId).orElse(null);
        int currentQuantity = item == null ? 0 : item.getQuantity();
        if (product.get().getStockQuantity() < currentQuantity + quantity) {
            return false;
        }

        if (item == null) {
            cart.addItem(NewCartItem.builder()
                    .product(product.get())
                    .quantity(quantity)
                    .build());
        } else {
            item.increaseQuantity(quantity);
        }

        return true;
    }

    private boolean updateDatabaseItem(Long userId, Long productId, int quantity) {
        if (userId == null || productId == null) {
            return false;
        }

        NewCart cart = getOrCreateDatabaseCart(userId);
        Optional<NewCartItem> item = findDatabaseItem(cart, productId);
        if (item.isEmpty()) {
            return false;
        }

        if (quantity <= 0) {
            cart.removeItem(item.get());
            return true;
        }

        Optional<Product> product = findProductIfValid(productId, quantity);
        if (product.isEmpty()) {
            return false;
        }

        item.get().setQuantity(quantity);
        return true;
    }

    private boolean removeDatabaseItem(Long userId, Long productId) {
        if (userId == null || productId == null) {
            return false;
        }

        NewCart cart = getOrCreateDatabaseCart(userId);
        Optional<NewCartItem> item = findDatabaseItem(cart, productId);
        item.ifPresent(cart::removeItem);
        return item.isPresent();
    }

    private void mergeSessionCartToDatabase(User user, HttpSession session) {
        if (user == null || session == null) {
            return;
        }

        Object cart = session.getAttribute(CART_SESSION_KEY);
        if (!(cart instanceof Cart sessionCart) || sessionCart.totalQuantity() == 0) {
            syncDatabaseCartCount(session, user.getId());
            return;
        }

        for (CartItem item : sessionCart.getItems()) {
            addDatabaseItem(user.getId(), item.getProduct().getId(), item.getQuantity());
        }

        session.removeAttribute(CART_SESSION_KEY);
        syncDatabaseCartCount(session, user.getId());
    }

    private Cart toSessionCart(NewCart databaseCart) {
        Cart cart = new Cart();
        for (NewCartItem item : databaseCart.getItems()) {
            cart.addItem(item.getProduct(), item.getQuantity());
        }
        return cart;
    }

    private Optional<NewCartItem> findDatabaseItem(NewCart cart, Long productId) {
        if (cart == null || cart.getItems() == null || productId == null) {
            return Optional.empty();
        }

        return cart.getItems()
                .stream()
                .filter(item -> item.getProduct() != null && productId.equals(item.getProduct().getId()))
                .findFirst();
    }

    private void syncDatabaseCartCount(HttpSession session, Long userId) {
        if (session == null || userId == null) {
            return;
        }

        session.setAttribute(CART_ITEM_COUNT_SESSION_KEY, getOrCreateDatabaseCart(userId).totalQuantity());
    }
}
