package nl.inholland.bank_api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(
        description = "Request body for registering a new user",
        requiredProperties = {"firstName", "lastName", "email", "password", "bsn", "phoneNumber"}
)
public class RegisterRequestDTO {
    @Schema(description = "User's first name", example = "John")
    @NotBlank(message = "First name is required")
    public String firstName;

    @Schema(description = "User's last name", example = "Doe")
    @NotBlank(message = "Last name is required")
    public String lastName;

    @Schema(description = "User's email address", example = "john.doe@example.com")
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    public String email;

    @Schema(description = "Password for the user account", example = "P@ssw0rd123")
    @NotBlank(message = "Password is required")
    public String password;

    @Schema(description = "User's BSN number (9 digits)", example = "123456789")
    @NotBlank(message = "BSN is required")
    @Pattern(regexp = "\\d{9}", message = "BSN must be exactly 9 digits")
    public String bsn;

    @Schema(description = "User's phone number (10–15 digits, optionally starts with +)", example = "+31612345678")
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone number must be 10–15 digits (optionally starts with +)")
    public String phoneNumber;
}
