package nl.inholland.bank_api.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class TransactionFilterDTO {

    private String startDate;
    private String endDate;
    private BigDecimal amount;
    private String comparison;
    private String sourceIban;
    private String targetIban;
}