package nl.inholland.bank_api.model.dto;

import nl.inholland.bank_api.model.enums.ApprovalStatus;
import lombok.Data;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Data
public class ApprovalStatusUpdateDTO {
    @NotNull
    private ApprovalStatus approvalStatus;
    private List<AccountDTO> accounts;
}