package nl.inholland.bank_api.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class TransactionRequestDTO {
    @JsonProperty("sourceAccount")
    private String sourceAccount;

    @JsonProperty("targetAccount")
    private String targetAccount;

    @JsonProperty("initiatedBy")
    private Long initiatedBy;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("description")
    private String description;

    // Getters and Setters
    public String getSourceAccount() {
        return sourceAccount;
    }

    public void setSourceAccount(String sourceAccount) {
        this.sourceAccount = sourceAccount;
    }

    public String getTargetAccount() {
        return targetAccount;
    }

    public void setTargetAccount(String targetAccount) {
        this.targetAccount = targetAccount;
    }

    public Long getInitiatedBy() {
        return initiatedBy;
    }

    public void setInitiatedBy(Long initiatedBy) {
        this.initiatedBy = initiatedBy;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}