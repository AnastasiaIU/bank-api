package nl.inholland.bank_api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import nl.inholland.bank_api.constant.ErrorMessages;
import nl.inholland.bank_api.constant.RegexPatterns;

@Schema(
        description = "Request body for registering a new user",
        requiredProperties = {"firstName", "lastName", "email", "password", "bsn", "phoneNumber"}
)
public class RegisterRequestDTO {
    @Schema(description = "User's first name", example = "John")
    @NotBlank(message = ErrorMessages.FIRST_NAME_REQUIRED)
    public String firstName;

    @Schema(description = "User's last name", example = "Doe")
    @NotBlank(message = ErrorMessages.LAST_NAME_REQUIRED)
    public String lastName;

    @Schema(description = "User's email address", example = "john.doe@example.com")
    @Email(message = ErrorMessages.INVALID_EMAIL_FORMAT)
    @NotBlank(message = ErrorMessages.EMAIL_REQUIRED)
    public String email;

    @Schema(description = "Password for the user account", example = "P@ssw0rd123")
    @NotBlank(message = ErrorMessages.PASSWORD_REQUIRED)
    public String password;

    @Schema(description = "User's BSN number (9 digits)", example = "123456789")
    @NotBlank(message = ErrorMessages.BSN_REQUIRED)
    @Pattern(regexp = RegexPatterns.BSN, message = ErrorMessages.INVALID_BSN_FORMAT)
    public String bsn;

    @Schema(description = "User's phone number (10–15 digits, optionally starts with +)", example = "+31612345678")
    @NotBlank(message = ErrorMessages.PHONE_REQUIRED)
    @Pattern(regexp = RegexPatterns.PHONE, message = ErrorMessages.INVALID_PHONE_FORMAT)
    public String phoneNumber;
}
