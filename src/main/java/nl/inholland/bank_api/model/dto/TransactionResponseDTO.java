package nl.inholland.bank_api.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponseDTO (
    Long id,
    Long sourceAccount,
    Long targetAccount,
    BigDecimal amount,
    String description,
    String status,
    String sourceIban,
    String targetIban,
    LocalDateTime timestamp,
    String failureReason
)
{}
