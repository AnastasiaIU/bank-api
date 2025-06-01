package nl.inholland.bank_api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;
import nl.inholland.bank_api.constant.ErrorMessages;

import java.math.BigDecimal;

@Data
@Schema(description = "DTO for updating account limit values.")
public class UpdateAccountLimitsDTO {
        @Schema(description = "The maximum amount the account holder can spend per day.",
                example = "1000.00")
        @DecimalMin(value = "0.01", message = ErrorMessages.DAILY_LIMIT_MINIMUM)
        private BigDecimal dailyLimit;

        @Schema(description = "The absolute minimum balance allowed for the account (can be negative).",
                example = "-500.00")
        private BigDecimal absoluteLimit;

        @Schema(description = "The maximum amount that can be withdrawn from an ATM",
                example = "300.00")
        @DecimalMin(value = "0.01", message = ErrorMessages.WITHDRAW_LIMIT_MINIMUM)
        private BigDecimal withdrawLimit;
}
