package nlu.fit.web.souvenirecommerce.legacy.dao.impl;

import nlu.fit.web.souvenirecommerce.common.base.AbsBaseRepository;
import nlu.fit.web.souvenirecommerce.legacy.dao.IRoleEntityIRepository;
import nlu.fit.web.souvenirecommerce.model.entity.Role;
import nlu.fit.web.souvenirecommerce.common.utils.HibernateUtil;

import java.util.List;
import java.util.Optional;

public class RoleIRepositoryImpl extends AbsBaseRepository<Long, Role> implements IRoleEntityIRepository {

    public RoleIRepositoryImpl() {
        super(Role.class);
    }

    @Override
    public List<Role> findAll() {
        String hql = """
                select distinct r from Role r
                left join fetch r.permissions
                order by r.name
                """;

        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(hql, Role.class).getResultList();
        }
    }

    @Override
    public Optional<Role> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }

        String hql = """
                select distinct r from Role r
                left join fetch r.permissions
                where r.id = :id
                """;

        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(hql, Role.class)
                    .setParameter("id", id)
                    .uniqueResultOptional();
        }
    }

    @Override
    public Optional<Role> findByName(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }

        String hql = """
                select distinct r from Role r
                left join fetch r.permissions
                where r.name = :name
                """;

        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(hql, Role.class)
                    .setParameter("name", name.trim())
                    .uniqueResultOptional();
        }
    }
}
