package nlu.fit.web.souvenirecommerce.legacy.dao.impl;

import nlu.fit.web.souvenirecommerce.common.base.AbsBaseRepository;
import nlu.fit.web.souvenirecommerce.legacy.dao.IUserCredentialIRepository;
import nlu.fit.web.souvenirecommerce.model.entity.UserCredential;
import nlu.fit.web.souvenirecommerce.common.utils.HibernateUtil;

import java.util.List;
import java.util.Optional;

public class IUserCredentialIRepositoryImpl extends AbsBaseRepository<Long, UserCredential> implements IUserCredentialIRepository {

    public IUserCredentialIRepositoryImpl() {
        super(UserCredential.class);
    }

    @Override
    public List<UserCredential> findAll() {
        String hql = """
                select uc from UserCredential uc
                join fetch uc.user
                order by uc.id desc
                """;

        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(hql, UserCredential.class).getResultList();
        }
    }

    @Override
    public Optional<UserCredential> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }

        String hql = """
                select uc from UserCredential uc
                join fetch uc.user
                where uc.id = :id
                """;

        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(hql, UserCredential.class)
                    .setParameter("id", id)
                    .uniqueResultOptional();
        }
    }

    @Override
    public Optional<UserCredential> findByVerificationToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        String hql = """
                select uc from UserCredential uc
                join fetch uc.user
                where uc.verificationToken = :token
                """;

        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(hql, UserCredential.class)
                    .setParameter("token", token.trim())
                    .uniqueResultOptional();
        }
    }

    @Override
    public Optional<UserCredential> findByResetToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        String hql = """
                select uc from UserCredential uc
                join fetch uc.user
                where uc.resetToken = :token
                """;

        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(hql, UserCredential.class)
                    .setParameter("token", token.trim())
                    .uniqueResultOptional();
        }
    }
}
