package nl.inholland.bank_api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import nl.inholland.bank_api.model.enums.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CombinedTransactionDTO {
    @Schema(example = "42", description = "Unique ID of the transaction")
    public Long id;
    @Schema(example = "ATM", description = "Type of transaction: ATM or TRANSFER")
    public String type; // "ATM" or "TRANSFER"
    @Schema(example = "NL91ABNA0417164300", description = "Source IBAN (if applicable)")
    public String sourceIban;
    @Schema(example = "NL82RABO0123456789", description = "Target IBAN (if applicable)")
    public String targetIban;
    @Schema(example = "100.00", description = "Amount of the transaction")
    public BigDecimal amount;
    @Schema(example = "Payment for groceries", description = "Description of the transaction")
    public String description;
    @Schema(example = "2025-06-07T14:55:23.123", description = "Timestamp of the transaction")
    public LocalDateTime timestamp;
    @Schema(example = "SUCCEEDED", description = "Transaction status")
    public Status status;
    @Schema(example = "Insufficient funds", description = "Reason for failure, if applicable")
    public String failureReason;
}
