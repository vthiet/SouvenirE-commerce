package nlu.fit.web.souvenirecommerce.features.product.repository;

import nlu.fit.web.souvenirecommerce.common.base.AbsBaseRepository;
import nlu.fit.web.souvenirecommerce.model.entity.Product;

import java.util.List;
import java.util.Optional;

public class ProductRepository extends AbsBaseRepository<Long, Product> {
    public ProductRepository() {
        super(Product.class);
    }

    public Optional<Product> findDetailById(Long productId) {
        if (productId == null) {
            return Optional.empty();
        }
        return getSession()
                .createQuery("""
                        select p
                        from Product p
                        left join fetch p.category
                        where p.id = :productId
                        """, Product.class)
                .setParameter("productId", productId)
                .uniqueResultOptional();
    }

    public List<Product> findRelatedProducts(Long categoryId, Long excludedProductId, int limit) {
        if (categoryId == null || excludedProductId == null || limit <= 0) {
            return List.of();
        }
        return getSession()
                .createQuery("""
                        select p
                        from Product p
                        left join fetch p.category
                        where p.category.id = :categoryId
                          and p.id <> :excludedProductId
                        order by p.totalSold desc, p.id desc
                        """, Product.class)
                .setParameter("categoryId", categoryId)
                .setParameter("excludedProductId", excludedProductId)
                .setMaxResults(limit)
                .getResultList();
    }
}
