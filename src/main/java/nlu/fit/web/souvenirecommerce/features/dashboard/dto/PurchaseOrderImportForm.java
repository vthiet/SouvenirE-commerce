package nlu.fit.web.souvenirecommerce.features.dashboard.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PurchaseOrderImportForm {
    private String supplierName;
    private String supplierTaxCode;
    private String supplierAddress;
    private String supplierPhone;
    private String supplierEmail;
    private String invoiceNumber;
    private LocalDate invoiceDate;
    private String contractNumber;
    private String deliveryNoteNumber;
    private String receivedBy;
    private BigDecimal vatRate;
    private String notes;
    private final List<PurchaseOrderItemForm> items = new ArrayList<>();
}
