package nlu.fit.web.souvenirecommerce.features.order.repository;

import nlu.fit.web.souvenirecommerce.common.base.AbsBaseRepository;
import nlu.fit.web.souvenirecommerce.model.entity.PaymentTransaction;

import java.util.Optional;

public class PaymentTransactionRepository extends AbsBaseRepository<Long, PaymentTransaction> {
    public PaymentTransactionRepository() {
        super(PaymentTransaction.class);
    }

    public Optional<PaymentTransaction> findByOrderId(Long orderId) {
        if (orderId == null) {
            return Optional.empty();
        }
        return getSession()
                .createQuery("""
                        select p
                        from PaymentTransaction p
                        join fetch p.order o
                        join fetch o.user
                        left join fetch o.status
                        where o.id = :orderId
                        """, PaymentTransaction.class)
                .setParameter("orderId", orderId)
                .uniqueResultOptional();
    }
}
