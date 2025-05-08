package nl.inholland.bank_api.model.dto;

import nl.inholland.bank_api.model.enums.UserRole;

public record UserProfileDTO(
        String firstName,
        String lastName,
        String email,
        String bsn,
        String phoneNumber,
        boolean isApproved,
        UserRole role
) {
}
