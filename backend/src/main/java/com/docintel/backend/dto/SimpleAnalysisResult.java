package com.docintel.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "DTO representing a document analysis request")
public class SimpleAnalysisResult {

    @Schema(description = "Customer name extracted from document")
    private String customerName;

    @Schema(description = "Invoice number extracted from document")
    private String invoiceNumber;

    @Schema(description = "Total amount extracted from document")
    private Double totalAmount;

    @Schema(description = "Invoice date extracted from document")
    private String invoiceDate;

    @Schema(description = "Due date extracted from document")
    private String dueDate;

    public SimpleAnalysisResult(String customerName, String invoiceNumber, Double totalAmount, String invoiceDate, String dueDate) {
        this.customerName = customerName;
        this.invoiceNumber = invoiceNumber;
        this.totalAmount = totalAmount;
        this.invoiceDate = invoiceDate;
        this.dueDate = dueDate;
    }
}
