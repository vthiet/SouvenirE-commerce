package nlu.fit.web.souvenirecommerce.features.cart.service;

import nlu.fit.web.souvenirecommerce.common.base.AbsBaseService;
import nlu.fit.web.souvenirecommerce.features.cart.model.NewCart;
import nlu.fit.web.souvenirecommerce.features.cart.repository.NewCartRepository;

import java.util.Optional;

public class NewCartService extends AbsBaseService<Long, NewCart> {
    private final NewCartRepository newCartRepository;

    public NewCartService() {
        this(new NewCartRepository());
    }

    public NewCartService(NewCartRepository repository) {
        super(repository);
        this.newCartRepository = repository;
    }

    public Optional<NewCart> findCartByUserId(Long userId) {
        return newCartRepository.findByUserId(userId);
    }

    public Optional<NewCart> mergeCart(Long userId, NewCart thatCart){
        NewCart cart = findCartByUserId(userId).orElse(new NewCart());
        cart.mergeItems(thatCart.getItems());

        return Optional.of(cart);
    }
}
