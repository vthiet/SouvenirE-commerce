package nlu.fit.web.souvenirecommerce.legacy.dao.impl;

import nlu.fit.web.souvenirecommerce.common.base.AbsBaseRepository;
import nlu.fit.web.souvenirecommerce.legacy.dao.ICategoryEntityIRepository;
import nlu.fit.web.souvenirecommerce.model.entity.Category;
import nlu.fit.web.souvenirecommerce.common.utils.HibernateUtil;
import org.hibernate.Transaction;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ICategoryIRepositoryImpl2 extends AbsBaseRepository<Long, Category> implements ICategoryEntityIRepository {

    public ICategoryIRepositoryImpl2() {
        super(Category.class);
    }

    @Override
    public List<Category> findAll() {
        String hql = """
                from Category c
                order by c.id desc
                """;

        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(hql, Category.class).getResultList();
        }
    }

    @Override
    public Optional<Category> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }

        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            return Optional.ofNullable(session.find(Category.class, id));
        }
    }

    @Override
    public List<Category> findHeaderCategories() {
        String hql = """
                from Category c
                order by c.id desc
                """;

        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(hql, Category.class).getResultList();
        }
    }

    @Override
    public List<Category> findTopSellingCategories(int limit) {
        if (limit <= 0) {
            return Collections.emptyList();
        }

        String hql = """
                select c
                from Category c
                join c.products p
                group by c
                order by sum(p.totalSold) desc
                """;

        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(hql, Category.class).setMaxResults(limit).getResultList();
        }
    }

    @Override
    public List<Long> findTopSellingCategoryIds(int limit) {
        if (limit <= 0) {
            return Collections.emptyList();
        }

        String hql = """
                select c.id
                from Category c
                join c.products p
                group by c
                order by sum(p.totalSold) desc
                """;

        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(hql, Long.class).setMaxResults(limit).getResultList();
        }
    }

    @Override
    public List<Category> findCategoriesNotIn(List<Long> usedIds) {
        if (usedIds == null || usedIds.isEmpty()) {
            return findAll();
        }

        String hql = """
                from Category c
                where c.id not in (:ids)
                order by c.id desc
                """;

        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(hql, Category.class).setParameter("ids", usedIds).getResultList();
        }
    }

    @Override
    public boolean insertCategory(Category category) {
        if (category == null) {
            return false;
        }

        Transaction transaction = null;

        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            session.persist(category);

            transaction.commit();
            return true;

        } catch (RuntimeException e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }

            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateCategory(Category category) {
        if (category == null || category.getId() == null) {
            return false;
        }

        Transaction transaction = null;

        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            session.merge(category);

            transaction.commit();
            return true;

        } catch (RuntimeException e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }

            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteCategory(Long id) {
        if (id == null) {
            return false;
        }

        Transaction transaction = null;

        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Category category = session.find(Category.class, id);

            if (category != null) {
                session.remove(category);
            }

            transaction.commit();
            return true;

        } catch (RuntimeException e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }

            e.printStackTrace();
            return false;
        }
    }

    @Override
    public int countProductsByCategory(Long categoryId) {
        if (categoryId == null) {
            return 0;
        }

        String hql = """
                select count(p.id)
                from Product p
                where p.category.id = :categoryId
                """;

        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery(hql, Long.class).setParameter("categoryId", categoryId).uniqueResult();

            return count != null ? count.intValue() : 0;
        }
    }
}