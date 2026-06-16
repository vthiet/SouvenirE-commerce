package nlu.fit.web.souvenirecommerce.features.cart.service;

import nlu.fit.web.souvenirecommerce.common.base.AbsBaseService;
import nlu.fit.web.souvenirecommerce.features.cart.model.CartEntity;
import nlu.fit.web.souvenirecommerce.features.cart.repository.CartEntityRepository;

import java.util.Optional;

public class CartEntityService extends AbsBaseService<Long, CartEntity> {
    private final CartEntityRepository cartEntityRepository;

    public CartEntityService() {
        this(new CartEntityRepository());
    }

    public CartEntityService(CartEntityRepository repository) {
        super(repository);
        this.cartEntityRepository = repository;
    }

    public Optional<CartEntity> findCartByUserId(Long userId) {
        return cartEntityRepository.findByUserId(userId);
    }

    public Optional<CartEntity> mergeCart(Long userId, CartEntity thatCart) {
        CartEntity cart = findCartByUserId(userId).orElse(new CartEntity());
        cart.mergeItems(thatCart.getItems());

        return Optional.of(cart);
    }
}
