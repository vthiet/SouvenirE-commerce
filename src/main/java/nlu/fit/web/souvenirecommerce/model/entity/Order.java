package nlu.fit.web.souvenirecommerce.model.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity(name = "CustomerOrder")
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "shipping_fee", precision = 15, scale = 2)
    private BigDecimal shippingFee;

    @Column(length = 1000)
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", nullable = false)
    private OrderStatus status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    /** Carrier shipments associated with this order (one per shipping attempt). */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ShippingOrder> shippingOrders = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    private List<PaymentTransaction> paymentTransactions;

    /**
     * The carrier code the customer selected at checkout.
     * Used as a hint when admin triggers shipping so the right provider is invoked.
     * Examples: "GHN", "GHTK".
     */
    @Column(name = "preferred_carrier_code", length = 50)
    private String preferredCarrierCode;

    public void addItem(OrderItem item) {
        if (items == null) {
            items = new ArrayList<>();
        }
        items.add(item);
        item.setOrder(this);
    }

    /**
     * Returns the most recently created carrier shipment for this order,
     * or {@code null} if no shipment has been created yet.
     */
    public ShippingOrder getActiveShippingOrder() {
        if (shippingOrders == null || shippingOrders.isEmpty()) {
            return null;
        }
        return shippingOrders.stream()
                .max(java.util.Comparator.comparingLong(so -> so.getId() == null ? 0L : so.getId()))
                .orElse(null);
    }

    public String getOrderCode() {
        if (id == null) {
            return null;
        }
        LocalDateTime codeDate = orderDate == null ? LocalDateTime.now() : orderDate;
        return "ORD-" + codeDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + String.format("%05d", id);
    }

    public Date getCreatedAt() {
        LocalDateTime dateTime = orderDate == null ? LocalDateTime.now() : orderDate;
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public String getStatusDescription() {
        return status == null ? "" : status.getDescription();
    }

    @jakarta.persistence.Transient
    public String getGhnStatus() {
        ShippingOrder active = getActiveShippingOrder();
        return active != null ? active.getStatus() : null;
    }

    @jakarta.persistence.Transient
    public String getPaymentMethod() {
        if (paymentTransactions != null && !paymentTransactions.isEmpty()) {
            PaymentTransaction lastTx = paymentTransactions.get(paymentTransactions.size() - 1);
            return lastTx.getMethod() != null ? lastTx.getMethod().name() : null;
        }
        return null;
    }

    @jakarta.persistence.Transient
    public Date getOrderDate() {
        return getCreatedAt();
    }

    @jakarta.persistence.Transient
    public String getCustomerName() {
        if (address != null && address.getReceiverName() != null && !address.getReceiverName().isBlank()) {
            return address.getReceiverName();
        }
        return user != null ? user.getFullName() : "";
    }

    @jakarta.persistence.Transient
    public String getCustomerEmail() {
        return user != null ? user.getEmail() : "";
    }


    @jakarta.persistence.Transient
    public String getStatus() {
        return getStatusDescription();
    }

    @PrePersist
    private void prePersist() {
        if (orderDate == null) {
            orderDate = LocalDateTime.now();
        }
    }
}
