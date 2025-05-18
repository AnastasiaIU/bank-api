package nl.inholland.bank_api.model.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class UpdateAccountLimitsDTO {
        private BigDecimal dailyLimit;
        private BigDecimal absoluteLimit;
        private BigDecimal withdrawLimit;
}
