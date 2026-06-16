package nlu.fit.web.souvenirecommerce.features.cart.repository;

import nlu.fit.web.souvenirecommerce.common.base.AbsBaseRepository;
import nlu.fit.web.souvenirecommerce.features.cart.model.CartEntity;
import nlu.fit.web.souvenirecommerce.model.entity.User;

import java.util.Optional;

public class CartEntityRepository extends AbsBaseRepository<Long, CartEntity> {

    public CartEntityRepository() {
        super(CartEntity.class);
    }

    @Override
    public Optional<CartEntity> findById(Long cartId) {
        if (cartId == null) return Optional.empty();

        CartEntity cart = getSession().createQuery("""
                select distinct nc
                from CartEntity nc
                left join fetch nc.items ci
                left join fetch ci.product p
                where nc.id = :cartId
                """, CartEntity.class).setParameter("cartId", cartId).uniqueResult();
        return Optional.ofNullable(cart);
    }

    public Optional<CartEntity> findByUserId(Long userId) {
        if (userId == null) return Optional.empty();

        CartEntity cart = getSession().createQuery("""
                select distinct nc
                from CartEntity nc
                left join fetch nc.items ci
                left join fetch ci.product p
                where nc.user.id = :userId
                """, CartEntity.class).setParameter("userId", userId).uniqueResult();

        return Optional.ofNullable(cart);
    }

    public CartEntity createForUser(Long userId) {
        User userRef = getSession().getReference(User.class, userId);
        CartEntity cart = CartEntity.builder()
                .user(userRef)
                .build();
        save(cart);
        return cart;
    }
}
