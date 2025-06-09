package nl.inholland.bank_api.model.dto;

import nl.inholland.bank_api.model.enums.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CombinedTransactionFullHistoryDTO {
    public String sourceIban;
    public String targetIban;
    public Long initiatedBy;
    public BigDecimal amount;
    public LocalDateTime timestamp;
    public String type;
    public Status status;

    public CombinedTransactionFullHistoryDTO(String sourceIban, String targetIban, Long initiatedBy, BigDecimal amount, LocalDateTime timestamp, String type, Status status) {
        this.sourceIban = sourceIban;
        this.targetIban = targetIban;
        this.initiatedBy = initiatedBy;
        this.amount = amount;
        this.timestamp = timestamp;
        this.type = type;
        this.status = status;
    }
}