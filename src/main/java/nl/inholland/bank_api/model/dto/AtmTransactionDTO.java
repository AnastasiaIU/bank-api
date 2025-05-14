package nl.inholland.bank_api.model.dto;

import nl.inholland.bank_api.model.enums.AtmTransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AtmTransactionDTO(
        Long id,
        String iban,
        Long initiatedBy,
        AtmTransactionType type,
        BigDecimal amount,
        LocalDateTime timestamp,
        String status,
        String failureReason
) {
}
