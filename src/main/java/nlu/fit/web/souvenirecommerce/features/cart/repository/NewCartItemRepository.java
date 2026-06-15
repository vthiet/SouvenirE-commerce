package nlu.fit.web.souvenirecommerce.features.cart.repository;

import nlu.fit.web.souvenirecommerce.common.base.AbsBaseRepository;
import nlu.fit.web.souvenirecommerce.features.cart.model.NewCartItem;

public class NewCartItemRepository extends AbsBaseRepository<Long, NewCartItem> {

    public NewCartItemRepository() {
        super(NewCartItem.class);
    }
}
