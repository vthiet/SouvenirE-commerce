package nlu.fit.web.souvenirecommerce.features.order.repository;

import nlu.fit.web.souvenirecommerce.common.base.AbsBaseRepository;
import nlu.fit.web.souvenirecommerce.model.entity.ShippingOrder;

import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link ShippingOrder} entities (table {@code shipping_orders}).
 */
public class ShippingOrderRepository extends AbsBaseRepository<Long, ShippingOrder> {

    public ShippingOrderRepository() {
        super(ShippingOrder.class);
    }

    /**
     * Returns the most recently created shipment for the given shop order,
     * or {@link Optional#empty()} if none exists yet.
     */
    public Optional<ShippingOrder> findLatestByOrderId(Long orderId) {
        if (orderId == null) {
            return Optional.empty();
        }
        return getSession()
                .createQuery("""
                        select so
                        from ShippingOrder so
                        where so.order.id = :orderId
                        order by so.id desc
                        """, ShippingOrder.class)
                .setParameter("orderId", orderId)
                .setMaxResults(1)
                .uniqueResultOptional();
    }

    /**
     * Returns order IDs whose active shipment is still in-transit and needs
     * a status sync from the carrier API.
     *
     * <p>Candidates are orders whose:
     * <ul>
     *   <li>order status is "Đang xử lý" or "Đang giao"</li>
     *   <li>latest shipment has a non-empty tracking code</li>
     *   <li>latest shipment status is not terminal (delivered / returned)</li>
     * </ul>
     *
     * @param limit maximum number of order IDs to return
     */
    public List<Long> findCarrierSyncCandidateOrderIds(int limit) {
        if (limit <= 0) {
            return List.of();
        }
        return getSession()
                .createQuery("""
                        select so.order.id
                        from ShippingOrder so
                        join so.order o
                        join o.status s
                        where so.trackingCode is not null
                          and so.trackingCode <> ''
                          and s.description in ('Đang xử lý', 'Đang giao')
                          and coalesce(lower(so.status), '') not in ('delivered', 'returned')
                          and so.id = (
                              select max(so2.id)
                              from ShippingOrder so2
                              where so2.order.id = so.order.id
                          )
                        order by coalesce(so.carrierUpdatedAt, o.orderDate) asc, so.order.id asc
                        """, Long.class)
                .setMaxResults(limit)
                .getResultList();
    }
}
