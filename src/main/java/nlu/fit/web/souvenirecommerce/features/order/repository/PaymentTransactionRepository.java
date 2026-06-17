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
                        where p.orderId = :orderId
                        """, PaymentTransaction.class)
                .setParameter("orderId", orderId)
                .uniqueResultOptional();
    }
}
