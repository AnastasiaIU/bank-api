package nl.inholland.bank_api.model.dto;

import nl.inholland.bank_api.model.enums.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponseDTO {
    public Long sourceAccount;
    public Long targetAccount;
    public BigDecimal amount;
    public String description;
    public Status status;
    public String sourceIban;
    public String targetIban;
    public LocalDateTime timestamp;
}