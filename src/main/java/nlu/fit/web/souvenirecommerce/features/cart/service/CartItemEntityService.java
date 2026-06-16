package nlu.fit.web.souvenirecommerce.features.cart.service;

import nlu.fit.web.souvenirecommerce.common.base.AbsBaseService;
import nlu.fit.web.souvenirecommerce.features.cart.model.CartItemEntity;
import nlu.fit.web.souvenirecommerce.features.cart.repository.CartItemEntityRepository;

public class CartItemEntityService extends AbsBaseService<Long, CartItemEntity> {

    public CartItemEntityService() {
        super(new CartItemEntityRepository());
    }
}
