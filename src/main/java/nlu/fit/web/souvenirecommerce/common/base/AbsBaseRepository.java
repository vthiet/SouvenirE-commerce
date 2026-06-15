package nlu.fit.web.souvenirecommerce.common.base;

import nlu.fit.web.souvenirecommerce.common.utils.HibernateUtil;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Optional;

public abstract class AbsBaseRepository<K, T> implements IRepository<K, T> {
    protected final Logger log = LoggerFactory.getLogger(getClass());
    private final Class<T> entityClass;

    public AbsBaseRepository(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public AbsBaseRepository() {
        this.entityClass = null;
    }

    protected Session getSession() {
        return HibernateUtil.getSessionFactory().getCurrentSession();
    }

    @Override
    public Optional<T> save(T entity) {
        try {
            getSession().persist(entity);
            getSession().flush();
            return Optional.of(entity);
        } catch (RuntimeException e) {
            log.error("Repository save failed for entity={}", simpleName(entity), e);
            throw e;
        }
    }

    @Override
    public Optional<T> update(T entity) {
        try {
            T merged = (T) getSession().merge(entity);
            return Optional.of(merged);
        } catch (RuntimeException e) {
            log.error("Repository update failed for entity={}", simpleName(entity), e);
            throw e;
        }
    }

    @Override
    public List<T> findAll() {
        try {
            Class<T> type = requireEntityClass();
            String hql = "from " + type.getSimpleName();
            return getSession().createQuery(hql, type).getResultList();
        } catch (RuntimeException e) {
            log.error("Repository findAll failed for entityClass={}", entityClass, e);
            throw e;
        }
    }

    @Override
    public Optional<T> findById(K id) {
        if (id == null) return Optional.empty();
        try {
            return Optional.ofNullable(getSession().find(requireEntityClass(), id));
        } catch (RuntimeException e) {
            log.error("Repository findById failed for entityClass={} id={}", entityClass, id, e);
            throw e;
        }
    }

    @Override
    public void delete(K id) {
        if (id == null) return;
        try {
            T entity = getSession().find(requireEntityClass(), id);
            if (entity != null) {
                getSession().remove(entity);
            }
        } catch (RuntimeException e) {
            log.error("Repository delete failed for entityClass={} id={}", entityClass, id, e);
            throw e;
        }
    }

    private Class<T> requireEntityClass() {
        if (entityClass == null) {
            throw new IllegalStateException("entityClass is not configured for repository " + getClass().getName());
        }
        return entityClass;
    }

    private String simpleName(T entity) {
        return entity == null ? "null" : entity.getClass().getSimpleName();
    }
}
