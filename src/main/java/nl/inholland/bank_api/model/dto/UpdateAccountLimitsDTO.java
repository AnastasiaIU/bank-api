package nl.inholland.bank_api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Schema(description = "DTO for updating account limit values.")
public class UpdateAccountLimitsDTO {
        @Schema(description = "The maximum amount the account holder can spend per day.",
                example = "1000.00")
        private BigDecimal dailyLimit;

        @Schema(description = "The absolute minimum balance allowed for the account (can be negative).",
                example = "-500.00")
        private BigDecimal absoluteLimit;

        @Schema(description = "The maximum amount that can be withdrawn from an ATM",
                example = "300.00")
        private BigDecimal withdrawLimit;
}
