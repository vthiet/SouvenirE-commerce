package nlu.fit.web.souvenirecommerce.common.base;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Abstract Base Service provides methods common to all services
 * @param <K> type of primary key
 * @param <T> type of entity
 */
public abstract class AbsBaseService<K, T> implements IService<K, T> {
    protected final Logger log = LoggerFactory.getLogger(getClass());
    @Getter
    @Setter
    protected IRepository<K, T> repository;

    public AbsBaseService(IRepository<K, T> repository) {
        this.repository = repository;
    }

    public AbsBaseService() {}

    @Override
    public Optional<T> save(T entity) {
        log.info("Request to save entity: {}", entityName(entity));
        return requireRepository().save(entity);
    }

    @Override
    public Optional<T> update(T entity) {
        log.info("Request to update entity: {}", entityName(entity));
        return requireRepository().update(entity);
    }

    @Override
    public List<T> findAll() {
        log.debug("Request to fetch all entities");
        return requireRepository().findAll();
    }

    @Override
    public Optional<T> findById(K id) {
        log.debug("Request to find entity by id: {}", id);
        return requireRepository().findById(id);
    }

    @Override
    public void delete(K id) {
        log.info("Request to delete entity with id: {}", id);
        requireRepository().delete(id);
    }

    @Override
    public long count() {
        // count bằng HQL "select count(e) from..."
        log.debug("Request to count entities");
        List<T> all = requireRepository().findAll();
        return all != null ? all.size() : 0;
    }

    private IRepository<K, T> requireRepository() {
        if (repository == null) {
            throw new IllegalStateException("Repository is not configured for service " + getClass().getName());
        }
        return repository;
    }

    private String entityName(T entity) {
        return entity == null ? "null" : entity.getClass().getSimpleName();
    }
}
