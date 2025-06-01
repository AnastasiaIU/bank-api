package nl.inholland.bank_api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import nl.inholland.bank_api.model.enums.ApprovalStatus;
import nl.inholland.bank_api.model.enums.UserRole;

@Schema(description = "User data including personal details and account status.")
public record UserProfileDTO(
        @Schema(description = "Unique identifier of the user", example = "4")
        Long id,

        @Schema(description = "User's first name", example = "John")
        String firstName,

        @Schema(description = "User's last name", example = "Doe")
        String lastName,

        @Schema(description = "User's email address", example = "johndoe@mail.com")
        String email,

        @Schema(description = "User's BSN (social security number)", example = "123456789")
        String bsn,

        @Schema(description = "User's phone number", example = "+31612345678")
        String phoneNumber,

        @Schema(description = "Approval status of the user", example = "APPROVED")
        ApprovalStatus isApproved,

        @Schema(description = "Role assigned to the user", example = "CUSTOMER")
        UserRole role
) {
}
