package nl.inholland.bank_api.model.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AccountDTO {
    public Long id;
    public Long userId;
    public String iban;
    public String type;
    public BigDecimal balance;
}