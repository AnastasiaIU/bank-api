package nl.inholland.bank_api.model.dto;

import java.math.BigDecimal;

public class TransactionDTO {
    public Long sourceAccount;
    public Long targetAccount;
    public BigDecimal amount;
    public String description;
}