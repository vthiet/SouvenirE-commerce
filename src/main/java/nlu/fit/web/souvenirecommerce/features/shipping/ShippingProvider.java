package nlu.fit.web.souvenirecommerce.features.shipping;

import nlu.fit.web.souvenirecommerce.model.entity.Address;
import nlu.fit.web.souvenirecommerce.model.entity.Order;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Contract that every shipping provider must implement.
 * Each provider encapsulates all communication with its external carrier API,
 * while the rest of the application works only against this interface.
 */
public interface ShippingProvider {

    /**
     * Stable, uppercase code that uniquely identifies this carrier.
     * Used as the {@code carrier_code} value stored in {@code shipping_orders}.
     * Examples: {@code "GHN"}, {@code "GHTK"}, {@code "VIETTEL_POST"}.
     */
    String getCode();

    /** Human-readable name of the carrier (e.g. "Giao Hàng Nhanh"). */
    String getName();

    /**
     * Returns location data (provinces / districts / wards) as a JSON string.
     * The format matches whatever the provider's own API returns so that
     * existing front-end parsers continue to work unchanged.
     *
     * @param type     one of {@code "province"}, {@code "district"}, {@code "ward"}
     * @param parentId the parent location ID as a string (province ID for districts,
     *                 district ID for wards, ignored for provinces)
     */
    String getLocations(String type, String parentId) throws IOException, InterruptedException;

    /** Calculates the shipping fee for the given delivery address. */
    BigDecimal calculateFee(Address address) throws IOException, InterruptedException;

    /**
     * Creates a carrier shipment for the order and returns the result.
     *
     * @return a {@link ShipmentResult} containing the tracking code and initial status
     */
    ShipmentResult createShipment(Order order) throws IOException, InterruptedException;

    /**
     * Fetches the latest status of an existing carrier shipment.
     *
     * @param trackingCode  the carrier tracking code (mã vận đơn)
     * @param currentStatus the status currently recorded locally (used for simulation)
     */
    ShipmentResult getShipmentStatus(String trackingCode, String currentStatus) throws IOException, InterruptedException;

    /**
     * Snapshot returned by {@link #createShipment} and {@link #getShipmentStatus}.
     */
    record ShipmentResult(
            String trackingCode,
            String status,
            LocalDateTime leadtime,
            LocalDateTime finishDate,
            LocalDateTime carrierUpdatedAt
    ) {}
}
