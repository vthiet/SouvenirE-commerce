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

    /**
     * @deprecated Moved to {@link ShippingOrderRepository#findCarrierSyncCandidateOrderIds(int)}.
     */
    @Deprecated
    public List<Long> findGhnSyncCandidateIds(int limit) {
        return new ShippingOrderRepository().findCarrierSyncCandidateOrderIds(limit);
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
                                when a.carrierDistrictId is not null
                                    and a.carrierWardCode is not null
                                    and a.carrierWardCode <> ''
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

    public double getTotalRevenue() {
        java.math.BigDecimal sum = getSession().createQuery("""
                select sum(o.totalAmount)
                from CustomerOrder o
                where o.status.description = 'Hoàn thành'
                """, java.math.BigDecimal.class)
                .uniqueResult();
        return sum != null ? sum.doubleValue() : 0.0;
    }

    public int getTotalOrders() {
        Long count = getSession().createQuery("""
                select count(o.id)
                from CustomerOrder o
                """, Long.class)
                .uniqueResult();
        return count != null ? count.intValue() : 0;
    }

    public List<Order> getRecentOrders(int limit) {
        return getSession().createQuery("""
                select distinct o
                from CustomerOrder o
                left join fetch o.status
                left join fetch o.user
                left join fetch o.address
                order by o.id desc
                """, Order.class)
                .setMaxResults(limit)
                .getResultList();
    }

    public List<Double> getMonthlyRevenueData(int monthsCount) {
        java.time.LocalDate startDate = java.time.LocalDate.now().minusMonths(monthsCount - 1L).withDayOfMonth(1);
        java.time.LocalDate endDate = java.time.LocalDate.now().plusMonths(1L).withDayOfMonth(1);

        List<Object[]> rows = getSession().createQuery("""
                select year(o.orderDate), month(o.orderDate), sum(o.totalAmount)
                from CustomerOrder o
                where o.status.description = 'Hoàn thành'
                  and o.orderDate >= :startDate
                  and o.orderDate < :endDate
                group by year(o.orderDate), month(o.orderDate)
                order by year(o.orderDate), month(o.orderDate)
                """, Object[].class)
                .setParameter("startDate", startDate.atStartOfDay())
                .setParameter("endDate", endDate.atStartOfDay())
                .getResultList();

        java.util.Map<String, Double> map = new java.util.HashMap<>();
        for (Object[] row : rows) {
            String key = row[0] + "-" + row[1];
            map.put(key, ((Number) row[2]).doubleValue());
        }

        List<Double> result = new java.util.ArrayList<>();
        java.time.LocalDate today = java.time.LocalDate.now();
        for (int i = monthsCount - 1; i >= 0; i--) {
            java.time.LocalDate m = today.minusMonths(i);
            String key = m.getYear() + "-" + m.getMonthValue();
            result.add(map.getOrDefault(key, 0.0));
        }
        return result;
    }

    public List<Integer> getMonthlyOrdersData(int monthsCount) {
        java.time.LocalDate startDate = java.time.LocalDate.now().minusMonths(monthsCount - 1L).withDayOfMonth(1);
        java.time.LocalDate endDate = java.time.LocalDate.now().plusMonths(1L).withDayOfMonth(1);

        List<Object[]> rows = getSession().createQuery("""
                select year(o.orderDate), month(o.orderDate), count(o.id)
                from CustomerOrder o
                where o.status.description = 'Hoàn thành'
                  and o.orderDate >= :startDate
                  and o.orderDate < :endDate
                group by year(o.orderDate), month(o.orderDate)
                order by year(o.orderDate), month(o.orderDate)
                """, Object[].class)
                .setParameter("startDate", startDate.atStartOfDay())
                .setParameter("endDate", endDate.atStartOfDay())
                .getResultList();

        java.util.Map<String, Integer> map = new java.util.HashMap<>();
        for (Object[] row : rows) {
            String key = row[0] + "-" + row[1];
            map.put(key, ((Number) row[2]).intValue());
        }

        List<Integer> result = new java.util.ArrayList<>();
        java.time.LocalDate today = java.time.LocalDate.now();
        for (int i = monthsCount - 1; i >= 0; i--) {
            java.time.LocalDate m = today.minusMonths(i);
            String key = m.getYear() + "-" + m.getMonthValue();
            result.add(map.getOrDefault(key, 0));
        }
        return result;
    }

    public java.util.Map<String, Integer> getOrderStatusCounts() {
        List<Object[]> rows = getSession().createQuery("""
                select o.status.description, count(o.id)
                from CustomerOrder o
                group by o.status.description
                order by count(o.id) desc
                """, Object[].class)
                .getResultList();

        java.util.Map<String, Integer> map = new java.util.LinkedHashMap<>();
        for (Object[] row : rows) {
            if (row[0] != null) {
                map.put((String) row[0], ((Number) row[1]).intValue());
            }
        }
        return map;
    }

    public List<Order> getOrdersByStatus(String status, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return getSession().createQuery("""
                select distinct o
                from CustomerOrder o
                left join fetch o.status
                left join fetch o.user
                left join fetch o.address
                where o.status.description = :status
                order by o.id desc
                """, Order.class)
                .setParameter("status", status)
                .setFirstResult(offset)
                .setMaxResults(pageSize)
                .getResultList();
    }

    public int getOrderCountByStatus(String status) {
        Long count = getSession().createQuery("""
                select count(o.id)
                from CustomerOrder o
                where o.status.description = :status
                """, Long.class)
                .setParameter("status", status)
                .uniqueResult();
        return count != null ? count.intValue() : 0;
    }

    public List<Order> getOrdersPaginated(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return getSession().createQuery("""
                select distinct o
                from CustomerOrder o
                left join fetch o.status
                left join fetch o.user
                left join fetch o.address
                order by o.id desc
                """, Order.class)
                .setFirstResult(offset)
                .setMaxResults(pageSize)
                .getResultList();
    }

    public List<Order> findUserOrders(Long userId, String statusFilter, String keyword) {
        StringBuilder jpql = new StringBuilder("""
            select distinct o
            from CustomerOrder o
            left join fetch o.status
            left join fetch o.items i
            left join fetch i.product p
            where o.user.id = :userId
        """);

        if (statusFilter != null && !statusFilter.isBlank() && !"all".equalsIgnoreCase(statusFilter)) {
            jpql.append(" and o.status.description = :statusFilter");
        }

        if (keyword != null && !keyword.isBlank()) {
            jpql.append("""
                 and (
                     str(o.id) like :keyword
                     or exists (
                         select 1 from OrderItem oi
                         where oi.order = o
                           and oi.productName like :keyword
                     )
                 )
            """);
        }

        jpql.append(" order by o.id desc");

        var query = getSession().createQuery(jpql.toString(), Order.class)
                .setParameter("userId", userId);

        if (statusFilter != null && !statusFilter.isBlank() && !"all".equalsIgnoreCase(statusFilter)) {
            query.setParameter("statusFilter", statusFilter);
        }

        if (keyword != null && !keyword.isBlank()) {
            query.setParameter("keyword", "%" + keyword.trim() + "%");
        }

        return query.getResultList();
    }

    public List<Object[]> getOrderStatusCountsByUserId(Long userId) {
        return getSession().createQuery("""
                select o.status.description, count(o.id)
                from CustomerOrder o
                where o.user.id = :userId
                  and o.status.description is not null
                group by o.status.description
                """, Object[].class)
                .setParameter("userId", userId)
                .getResultList();
    }
}
