package nlu.fit.web.souvenirecommerce.dao;

import nlu.fit.web.souvenirecommerce.dto.ReviewSummary;
import nlu.fit.web.souvenirecommerce.model.entity.Review;
import nlu.fit.web.souvenirecommerce.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReviewDAO {

    public List<Review> getReviewsByProductWithFilter(
            int productId,
            Integer rating,
            String sort,
            int offset,
            int limit
    ) {

        try (Session session =
                     HibernateUtil.getSessionFactory().openSession()) {

            StringBuilder hql = new StringBuilder("""
                SELECT r
                FROM Review r
                JOIN FETCH r.user
                WHERE r.product.id = :productId
            """);

            if (rating != null) {
                hql.append(" AND r.rating = :rating ");
            }

            if ("oldest".equalsIgnoreCase(sort)) {
                hql.append(" ORDER BY r.createdAt ASC ");
            } else {
                hql.append(" ORDER BY r.createdAt DESC ");
            }

            var query = session.createQuery(hql.toString(), Review.class);

            query.setParameter("productId", productId);

            if (rating != null) {
                query.setParameter("rating", rating);
            }

            query.setFirstResult(offset);
            query.setMaxResults(limit);

            return query.list();
        }
    }

    public ReviewSummary getReviewSummaryByProductId(int productId) {

        try (Session session =
                     HibernateUtil.getSessionFactory().openSession()) {

            Object[] result = session.createQuery("""
                SELECT COUNT(r), AVG(r.rating)
                FROM Review r
                WHERE r.product.id = :productId
            """, Object[].class)
                    .setParameter("productId", productId)
                    .uniqueResult();

            Long total = (Long) result[0];

            Double avg = result[1] != null
                    ? ((Double) result[1])
                    : 0.0;

            return new ReviewSummary(
                    total.intValue(),
                    Math.round(avg * 10.0) / 10.0
            );
        }
    }

    public boolean hasPurchased(int userId, int productId) {

        try (Session session =
                     HibernateUtil.getSessionFactory().openSession()) {

            Long count = session.createQuery("""
                SELECT COUNT(oi)
                FROM OrderItem oi
                WHERE oi.order.user.id = :userId
                  AND oi.product.id = :productId
            """, Long.class)
                    .setParameter("userId", userId)
                    .setParameter("productId", productId)
                    .uniqueResult();

            return count != null && count > 0;
        }
    }

    public Map<Integer, Long> countReviewsByRating(int productId) {

        Map<Integer, Long> map = new HashMap<>();

        for (int i = 1; i <= 5; i++) {
            map.put(i, 0L);
        }

        try (Session session =
                     HibernateUtil.getSessionFactory().openSession()) {

            List<Object[]> results = session.createQuery("""
                SELECT r.rating, COUNT(r)
                FROM Review r
                WHERE r.product.id = :productId
                GROUP BY r.rating
            """, Object[].class)
                    .setParameter("productId", productId)
                    .list();

            for (Object[] row : results) {

                Integer reviewRating = (Integer) row[0];
                Long count = (Long) row[1];

                map.put(reviewRating, count);
            }
        }

        return map;
    }

    public boolean addReview(Review review) {

        Transaction tx = null;

        try (Session session =
                     HibernateUtil.getSessionFactory().openSession()) {

            tx = session.beginTransaction();

            session.persist(review);

            tx.commit();

            return true;

        } catch (Exception e) {

            if (tx != null) {
                tx.rollback();
            }

            e.printStackTrace();

            return false;
        }
    }
}