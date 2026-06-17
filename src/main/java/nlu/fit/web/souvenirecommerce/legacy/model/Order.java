package nlu.fit.web.souvenirecommerce.legacy.model;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    private int id;
    private int userId;
    private String customerName;
    private String customerEmail;
    private Date orderDate;
    private double totalAmount;
    private double shippingFee;
    private String status;
    private String shippingAddress;
    private String paymentMethod;
    private String ghnOrderCode;
    private String ghnStatus;
    private Date ghnUpdatedAt;
    private Date ghnLeadtime;
    private Date ghnFinishDate;
}
