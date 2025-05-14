package nl.inholland.bank_api.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import nl.inholland.bank_api.model.enums.AtmTransactionType;

import java.math.BigDecimal;

public class AtmTransactionRequestDTO {
    @NotBlank(message = "Account number (IBAN) is required")
    @Pattern(regexp = "^[A-Z]{2}[0-9]{2}[A-Z0-9]{11,30}$", message = "Invalid IBAN format")
    public String iban;

    @NotNull(message = "Transaction type is required")
    public AtmTransactionType type;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    public BigDecimal amount;
}
