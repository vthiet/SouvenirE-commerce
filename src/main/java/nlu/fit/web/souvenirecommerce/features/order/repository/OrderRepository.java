package nlu.fit.web.souvenirecommerce.features.order.repository;

import nlu.fit.web.souvenirecommerce.common.base.AbsBaseRepository;
import nlu.fit.web.souvenirecommerce.model.entity.Address;
import nlu.fit.web.souvenirecommerce.model.entity.Order;

import java.util.List;
import java.util.Optional;

public class OrderRepository extends AbsBaseRepository<Long, Order> {
    public OrderRepository() {
        super(Order.class);
    }

    @Override
    public List<Order> findAll() {
        return getSession()
                .createQuery("from CustomerOrder o order by o.id desc", Order.class)
                .getResultList();
    }

    public List<Order> findByUserId(Long userId) {
        if (userId == null) {
            return List.of();
        }
        return getSession()
                .createQuery("""
                        select distinct o
                        from CustomerOrder o
                        left join fetch o.status
                        where o.user.id = :userId
                        order by o.id desc
                        """, Order.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    public Optional<Address> findLatestShippingAddressByUserId(Long userId) {
        if (userId == null) {
            return Optional.empty();
        }

        List<Address> addresses = getSession()
                .createQuery("""
                        select a
                        from CustomerOrder o
                        join o.address a
                        where o.user.id = :userId
                        order by
                            case
                                when a.ghnDistrictId is not null
                                    and a.ghnWardCode is not null
                                    and a.ghnWardCode <> ''
                                then 0
                                else 1
                            end,
                            o.orderDate desc,
                            o.id desc
                        """, Address.class)
                .setParameter("userId", userId)
                .setMaxResults(1)
                .getResultList();

        return addresses.stream().findFirst();
    }
}
