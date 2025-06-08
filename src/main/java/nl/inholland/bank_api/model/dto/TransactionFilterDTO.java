package nl.inholland.bank_api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class TransactionFilterDTO {

    @Schema(example= "2025-06-02", description = "Start date filter in yyyy-MM-dd format")
    private String startDate;
    @Schema(example = "2025-06-30", description = "End date filter in yyyy-MM-dd format")
    private String endDate;
    @Schema(example = "100.00", description = "Amount to filter by")
    private BigDecimal amount;
    @Schema(example = "gt", description = "Comparison operator: lt (less), eq (equal), gt (greater)")
    private String comparison;
    @Schema(example = "NL01INHO0123456789", description = "Filter by source IBAN")
    private String sourceIban;
    @Schema(example = "NL01INHO0987654321", description = "Filter by target IBAN")
    private String targetIban;
    @Schema(example = "groceries", description = "Filter transactions by partial description match (case-insensitive)")
    private String description;
}