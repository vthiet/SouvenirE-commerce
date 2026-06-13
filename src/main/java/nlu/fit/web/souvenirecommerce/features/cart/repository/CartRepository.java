package nlu.fit.web.souvenirecommerce.features.cart.repository;

import nlu.fit.web.souvenirecommerce.common.base.AbsBaseRepository;
import nlu.fit.web.souvenirecommerce.model.entity.Product;

import java.util.Optional;

public class CartRepository extends AbsBaseRepository<Long, Product> {
    public CartRepository() {
        super(Product.class);
    }

    public Optional<Product> findAvailableProductById(Long productId) {
        if (productId == null) {
            return Optional.empty();
        }

        return getSession()
                .createQuery("""
                        select p
                        from Product p
                        where p.id = :productId
                        """, Product.class)
                .setParameter("productId", productId)
                .uniqueResultOptional();
    }
}
