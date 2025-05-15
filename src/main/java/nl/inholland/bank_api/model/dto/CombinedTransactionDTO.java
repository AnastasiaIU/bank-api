package nl.inholland.bank_api.model.dto;

import nl.inholland.bank_api.model.enums.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CombinedTransactionDTO {
    public Long id;
    public String type; // "ATM" or "TRANSFER"
    public String sourceIban;
    public String targetIban;
    public BigDecimal amount;
    public String description;
    public LocalDateTime timestamp;
    public Status status;
    public String failureReason;
}
