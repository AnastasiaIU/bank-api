package nl.inholland.bank_api.model.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Account data combined with basic user information.")
public class AccountWithUserDTO {
    @Schema(description = "The International Bank Account Number (IBAN) of the account.", example = "NL91ABNA0417164300")
    public String iban;

    @Schema(description = "The type of account, e.g., CHECKING, SAVINGS.", example = "CHECKING")
    public String type;

    @Schema(description = "The current balance of the account.", example = "3500.75")
    public BigDecimal balance;

    @Schema(description = "First name of the account owner.", example = "John")
    public String firstName;

    @Schema(description = "Last name of the account owner.", example = "Doe")
    public String lastName;

    @Schema(description = "The maximum amount the user can spend per day.", example = "1000.00")
    public BigDecimal dailyLimit;

    @Schema(description = "The absolute minimum balance the account can go to", example = "-100.00")
    public BigDecimal absoluteLimit;

    @Schema(description = "The maximum amount that can be withdrawn from the ATM", example = "500.00")
    public BigDecimal withdrawLimit;
}
