package nlu.fit.web.souvenirecommerce.features.dashboard.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PurchaseOrderSummaryDTO {
    private Long id;
    private String poCode;
    private String status;
    private String statusLabel;
    private String supplierName;
    private String supplierTaxCode;
    private String supplierAddress;
    private String supplierPhone;
    private String supplierEmail;
    private String invoiceNumber;
    private LocalDate invoiceDate;
    private String invoiceDateDisplay;
    private String contractNumber;
    private String deliveryNoteNumber;
    private String receivedBy;
    private Integer itemCount;
    private Integer totalQuantity;
    private BigDecimal subtotalAmount;
    private BigDecimal vatRate;
    private BigDecimal vatAmount;
    private BigDecimal totalAmount;
    private String createdByLabel;
    private String createdAtDisplay;
    private String notes;
    private List<PurchaseOrderItemSummaryDTO> items = new ArrayList<>();
}
