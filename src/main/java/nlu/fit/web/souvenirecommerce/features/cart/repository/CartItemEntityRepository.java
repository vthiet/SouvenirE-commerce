package nlu.fit.web.souvenirecommerce.features.cart.repository;

import nlu.fit.web.souvenirecommerce.common.base.AbsBaseRepository;
import nlu.fit.web.souvenirecommerce.features.cart.model.CartItemEntity;

public class CartItemEntityRepository extends AbsBaseRepository<Long, CartItemEntity> {

    public CartItemEntityRepository() {
        super(CartItemEntity.class);
    }
}
