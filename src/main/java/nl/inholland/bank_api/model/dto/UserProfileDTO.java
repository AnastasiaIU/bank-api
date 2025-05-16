package nl.inholland.bank_api.model.dto;

import nl.inholland.bank_api.model.enums.ApprovalStatus;
import nl.inholland.bank_api.model.enums.UserRole;

public record UserProfileDTO(
        Long id,
        String firstName,
        String lastName,
        String email,
        String bsn,
        String phoneNumber,
        ApprovalStatus isApproved,
        UserRole role
) {
}
