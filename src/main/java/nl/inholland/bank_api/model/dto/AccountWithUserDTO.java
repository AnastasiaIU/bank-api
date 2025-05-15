package nl.inholland.bank_api.model.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class AccountWithUserDTO {
    public String iban;
    public String type;
    public BigDecimal balance;
    public String firstName;
    public String lastName;
}
