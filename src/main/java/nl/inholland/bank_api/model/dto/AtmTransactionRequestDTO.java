package nl.inholland.bank_api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import nl.inholland.bank_api.model.enums.AtmTransactionType;

import java.math.BigDecimal;

@Schema(
        description = "Request body for ATM transaction operations",
        requiredProperties = {"iban", "type", "amount"}
)
public class AtmTransactionRequestDTO {
    @Schema(example = "NL01INHO0123456789", description = "IBAN of the user's account")
    @NotBlank(message = "Account number (IBAN) is required")
    @Pattern(regexp = "^[A-Z]{2}[0-9]{2}[A-Z0-9]{11,30}$", message = "Invalid IBAN format")
    public String iban;

    @Schema(example = "WITHDRAW", description = "Transaction type (WITHDRAW or DEPOSIT)")
    @NotNull(message = "Transaction type is required")
    public AtmTransactionType type;

    @Schema(example = "250.75", description = "Amount to deposit or withdraw")
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    public BigDecimal amount;
}
