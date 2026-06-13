package nlu.fit.web.souvenirecommerce.legacy.dao.impl;

import nlu.fit.web.souvenirecommerce.common.base.AbsBaseRepository;
import nlu.fit.web.souvenirecommerce.legacy.dao.IOAuthAccountEntityIRepository;
import nlu.fit.web.souvenirecommerce.model.entity.OAuthAccount;
import nlu.fit.web.souvenirecommerce.common.utils.HibernateUtil;

import java.util.List;
import java.util.Optional;

public class OAuthAccountIRepositoryImpl extends AbsBaseRepository<Long, OAuthAccount> implements IOAuthAccountEntityIRepository {

    public OAuthAccountIRepositoryImpl() {
        super(OAuthAccount.class);
    }

    @Override
    public List<OAuthAccount> findAll() {
        String hql = """
                select oa from OAuthAccount oa
                join fetch oa.user
                order by oa.id desc
                """;

        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(hql, OAuthAccount.class).getResultList();
        }
    }

    @Override
    public Optional<OAuthAccount> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }

        String hql = """
                select oa from OAuthAccount oa
                join fetch oa.user
                where oa.id = :id
                """;

        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(hql, OAuthAccount.class)
                    .setParameter("id", id)
                    .uniqueResultOptional();
        }
    }

    @Override
    public Optional<OAuthAccount> findByProviderAndProviderUserId(String provider, String providerUserId) {
        if (provider == null || provider.isBlank() || providerUserId == null || providerUserId.isBlank()) {
            return Optional.empty();
        }

        String hql = """
                select oa from OAuthAccount oa
                join fetch oa.user
                where oa.provider = :provider
                  and oa.providerUserId = :providerUserId
                """;

        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(hql, OAuthAccount.class)
                    .setParameter("provider", provider.trim())
                    .setParameter("providerUserId", providerUserId.trim())
                    .uniqueResultOptional();
        }
    }
}
