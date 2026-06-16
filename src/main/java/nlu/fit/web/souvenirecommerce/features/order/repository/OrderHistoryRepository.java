package nlu.fit.web.souvenirecommerce.features.order.repository;

import nlu.fit.web.souvenirecommerce.common.base.AbsBaseRepository;
import nlu.fit.web.souvenirecommerce.model.entity.OrderHistory;
import java.util.List;

public class OrderHistoryRepository extends AbsBaseRepository<Long, OrderHistory> {
    public OrderHistoryRepository() {
        super(OrderHistory.class);
    }

    public List<OrderHistory> findByOrderId(Long orderId) {
        if (orderId == null) {
            return List.of();
        }
        return getSession()
                .createQuery("from OrderHistory h where h.order.id = :orderId order by h.createdAt asc, h.id asc", OrderHistory.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }
}
