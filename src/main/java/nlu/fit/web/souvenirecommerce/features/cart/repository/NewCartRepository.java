package nlu.fit.web.souvenirecommerce.features.cart.repository;

import nlu.fit.web.souvenirecommerce.common.base.AbsBaseRepository;
import nlu.fit.web.souvenirecommerce.features.cart.model.NewCart;
import nlu.fit.web.souvenirecommerce.model.entity.User;

import java.util.Optional;

public class NewCartRepository extends AbsBaseRepository<Long, NewCart> {

    public NewCartRepository() {
        super(NewCart.class);
    }

    @Override
    public Optional<NewCart> findById(Long cartId) {
        if (cartId == null) return Optional.empty();

        NewCart cart = getSession().createQuery("""
                select distinct nc
                from NewCart nc
                left join fetch nc.items ci
                left join fetch ci.product p
                where nc.id = :cartId
                """, NewCart.class).setParameter("cartId", cartId).uniqueResult();
        return Optional.ofNullable(cart);
    }

    public Optional<NewCart> findByUserId(Long userId) {
        if (userId == null) return Optional.empty();

        NewCart cart = getSession().createQuery("""
                select distinct nc
                from NewCart nc
                left join fetch nc.items ci
                left join fetch ci.product p
                where nc.user.id = :userId
                """, NewCart.class).setParameter("userId", userId).uniqueResult();

        return Optional.ofNullable(cart);
    }

    public NewCart createForUser(Long userId) {
        User userRef = getSession().getReference(User.class, userId);
        NewCart cart = NewCart.builder()
                .user(userRef)
                .build();
        save(cart);
        return cart;
    }
}
