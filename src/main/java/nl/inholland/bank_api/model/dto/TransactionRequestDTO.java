package nl.inholland.bank_api.model.dto;

import java.math.BigDecimal;

public class TransactionRequestDTO {
    public String sourceAccount;
    public String targetAccount;
    public BigDecimal amount;
    public String description;
}