package nl.inholland.bank_api.model.dto;

import nl.inholland.bank_api.model.enums.AtmTransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AtmHistoryTransactionDTO {
    public String iban;
    public String initiatedBy;
    public AtmTransactionType type;
    public BigDecimal amount;
    public LocalDateTime timestamp;

    public AtmHistoryTransactionDTO(String iban, String initiatedBy, AtmTransactionType type, BigDecimal amount, LocalDateTime timestamp) {
        this.iban = iban;
        this.initiatedBy = initiatedBy;
        this.type = type;
        this.amount = amount;
        this.timestamp = timestamp;
    }
}