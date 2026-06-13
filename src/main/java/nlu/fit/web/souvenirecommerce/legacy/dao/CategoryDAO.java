package nlu.fit.web.souvenirecommerce.legacy.dao;

import nlu.fit.web.souvenirecommerce.model.entity.Category;
import nlu.fit.web.souvenirecommerce.common.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class CategoryDAO {
    // User
    public List<Category> getAllCategories() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Category c ORDER BY c.id DESC";
            return session.createQuery(hql, Category.class).list();
        }
    }

    public Category getCategoryById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.find(Category.class, id);
        }
    }

    public List<Category> getTopSellingCategories(int limit) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = """
                SELECT c FROM Category c
                JOIN c.products p
                GROUP BY c
                ORDER BY SUM(p.totalSold) DESC
            """;

            return session.createQuery(hql, Category.class)
                    .setMaxResults(limit)
                    .list();
        }
    }

    public List<Long> getTopSellingCategoryIds(int limit) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = """
                SELECT c.id FROM Category c
                JOIN c.products p
                GROUP BY c
                ORDER BY SUM(p.totalSold) DESC
            """;

            return session.createQuery(hql, Long.class)
                    .setMaxResults(limit)
                    .list();
        }
    }

    public List<Category> getCategoriesNotIn(List<Long> usedIds) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            if (usedIds == null || usedIds.isEmpty()) {
                return getAllCategories();
            }

            String hql = "FROM Category c WHERE c.id NOT IN (:ids)";
            return session.createQuery(hql, Category.class)
                    .setParameter("ids", usedIds)
                    .list();
        }
    }

    // Admin

    public boolean insertCategory(Category category) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(category);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateCategory(Category category) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(category);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteCategory(Long id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            Category category = session.find(Category.class, id);
            if (category != null) {
                session.remove(category);
            }

            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return false;
        }
    }

    public int getProductCountByCategory(Long categoryId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT COUNT(p.id) FROM Product p WHERE p.category.id = :categoryId";

            Long count = session.createQuery(hql, Long.class)
                    .setParameter("categoryId", categoryId)
                    .uniqueResult();

            return count != null ? count.intValue() : 0;
        }
    }
}