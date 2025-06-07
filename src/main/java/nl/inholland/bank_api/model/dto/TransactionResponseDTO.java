package nl.inholland.bank_api.model.dto;

import nl.inholland.bank_api.model.entities.Account;
import nl.inholland.bank_api.model.enums.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponseDTO {
    public Long sourceAccount;
    public Long targetAccount;
    public String initiatedBy;
    public BigDecimal amount;
    public String description;
    public Status status;
    public String sourceIban;
    public String targetIban;
    public LocalDateTime timestamp;

    public TransactionResponseDTO(Account sourceAccount, Account targetAccount, String initiatedBy, BigDecimal amount, String description, Status status, LocalDateTime timestamp) {
        this.sourceAccount = sourceAccount.getId();
        this.targetAccount = targetAccount.getId();
        this.initiatedBy = initiatedBy;
        this.amount = amount;
        this.description = description;
        this.status = status;
        this.sourceIban = sourceAccount.getIban();
        this.targetIban = targetAccount.getIban();
        this.timestamp = timestamp;
    }
}