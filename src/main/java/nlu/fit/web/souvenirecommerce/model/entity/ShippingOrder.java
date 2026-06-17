package nlu.fit.web.souvenirecommerce.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Represents a carrier shipment for a shop order.
 * Separating this from {@link Order} allows an order to be re-shipped
 * by the same or a different carrier, and keeps the carrier identity
 * (e.g., "GHN", "GHTK") explicit at all times.
 */
@Entity
@Table(name = "shipping_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The shop order this shipment belongs to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /**
     * Short code identifying the carrier/shipping provider.
     * Examples: "GHN", "GHTK", "VIETTEL_POST".
     */
    @Column(name = "carrier_code", length = 50, nullable = false)
    private String carrierCode;

    /** Tracking number assigned by the carrier (mã vận đơn). */
    @Column(name = "tracking_code", length = 100)
    private String trackingCode;

    /** Latest status string returned by the carrier API. */
    @Column(name = "status", length = 50)
    private String status;

    /** Estimated delivery time provided by the carrier. */
    @Column(name = "leadtime")
    private LocalDateTime leadtime;

    /** Actual delivery finish date once the carrier marks it done. */
    @Column(name = "finish_date")
    private LocalDateTime finishDate;

    /** Timestamp of the last status update received from the carrier. */
    @Column(name = "carrier_updated_at")
    private LocalDateTime carrierUpdatedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
