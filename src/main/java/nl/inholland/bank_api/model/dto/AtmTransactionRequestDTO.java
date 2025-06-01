package nl.inholland.bank_api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import nl.inholland.bank_api.constant.ErrorMessages;
import nl.inholland.bank_api.constant.RegexPatterns;
import nl.inholland.bank_api.model.enums.AtmTransactionType;

import java.math.BigDecimal;

@Schema(
        description = "Request body for ATM transaction operations",
        requiredProperties = {"iban", "type", "amount"}
)
public class AtmTransactionRequestDTO {
    @Schema(example = "NL01INHO0123456789", description = "IBAN of the user's account")
    @NotBlank(message = ErrorMessages.IBAN_REQUIRED)
    @Pattern(regexp = RegexPatterns.IBAN, message = ErrorMessages.INVALID_IBAN_FORMAT)
    public String iban;

    @Schema(example = "WITHDRAW", description = "Transaction type (WITHDRAW or DEPOSIT)")
    @NotNull(message = ErrorMessages.TRANSACTION_TYPE_REQUIRED)
    public AtmTransactionType type;

    @Schema(example = "250.75", description = "Amount to deposit or withdraw")
    @NotNull(message = ErrorMessages.AMOUNT_REQUIRED)
    @DecimalMin(value = "0.01", message = ErrorMessages.AMOUNT_MINIMUM)
    public BigDecimal amount;
}
