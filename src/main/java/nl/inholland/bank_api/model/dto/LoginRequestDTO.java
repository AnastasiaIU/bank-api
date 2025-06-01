package nl.inholland.bank_api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import nl.inholland.bank_api.constant.ErrorMessages;

@Schema(description = "Login request containing user email and password.")
public class LoginRequestDTO {
    @Email(message = ErrorMessages.INVALID_EMAIL_FORMAT)
    @NotBlank(message = ErrorMessages.EMAIL_REQUIRED)
    @Schema(description = "User's email address", example = "johndoe@mail.com")
    public String email;

    @NotBlank(message = ErrorMessages.PASSWORD_REQUIRED)
    @Schema(description = "User's password", example = "P@ssword123")
    public String password;
}
