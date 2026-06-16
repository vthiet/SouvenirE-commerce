package nlu.fit.web.souvenirecommerce.features.shipping;

import nlu.fit.web.souvenirecommerce.model.entity.Address;
import nlu.fit.web.souvenirecommerce.model.entity.Order;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface ShippingProvider {
    String getName();
    
    BigDecimal calculateFee(Address address) throws IOException, InterruptedException;
    
    ShippingOrderResult createOrder(Order order) throws IOException, InterruptedException;
    
    ShippingOrderResult getOrderDetail(String trackingCode, String currentStatus) throws IOException, InterruptedException;

    record ShippingOrderResult(
            String orderCode, 
            String status, 
            LocalDateTime leadtime,
            LocalDateTime finishDate, 
            LocalDateTime updatedAt
    ) {}
}
