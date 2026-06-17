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

    public List<Product> findAllForStockImport() {
        return getSession()
                .createQuery("""
                        select p
                        from Product p
                        left join fetch p.category
                        order by p.stockQuantity asc, p.name asc, p.id desc
                        """, Product.class)
                .getResultList();
    }

    public List<Product> findLowStockProducts(int threshold, int limit) {
        if (threshold < 0 || limit <= 0) {
            return List.of();
        }
        return getSession()
                .createQuery("""
                        select p
                        from Product p
                        left join fetch p.category
                        where p.stockQuantity <= :threshold
                        order by p.stockQuantity asc, p.name asc, p.id desc
                        """, Product.class)
                .setParameter("threshold", threshold)
                .setMaxResults(limit)
                .getResultList();
    }

    public long countLowStockProducts(int threshold) {
        Long count = getSession()
                .createQuery("""
                        select count(p)
                        from Product p
                        where p.stockQuantity <= :threshold
                        """, Long.class)
                .setParameter("threshold", threshold)
                .uniqueResult();
        return count == null ? 0L : count;
    }
}
