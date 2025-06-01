package nl.inholland.bank_api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Login request containing user email and password.")
public class LoginRequestDTO {
    @NotBlank(message = "Email is required")
    @Email(message = "Incorrect email format")
    @Schema(description = "User's email address", example = "johndoe@mail.com")
    public String email;

    @NotBlank(message = "Password is required")
    @Schema(description = "User's password", example = "P@ssword123")
    public String password;
}
