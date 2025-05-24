package nl.inholland.bank_api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import nl.inholland.bank_api.model.enums.AtmTransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Details of an ATM transaction")
public record AtmTransactionDTO(
        @Schema(description = "Unique ID of the transaction", example = "42")
        Long id,

        @Schema(description = "IBAN of the associated account", example = "NL91ABNA0417164300")
        String iban,

        @Schema(description = "ID of the user who initiated the transaction", example = "7")
        Long initiatedBy,

        @Schema(description = "Type of ATM transaction", example = "WITHDRAW")
        AtmTransactionType type,

        @Schema(description = "Transaction amount in EUR", example = "150.55")
        BigDecimal amount,

        @Schema(description = "Timestamp when the transaction was created", example = "2025-05-24 21:40:02.455569")
        LocalDateTime timestamp,

        @Schema(description = "Current status of the transaction", example = "SUCCEEDED")
        String status,

        @Schema(description = "Reason the transaction failed, if any", example = "Insufficient funds")
        String failureReason
) {
}
