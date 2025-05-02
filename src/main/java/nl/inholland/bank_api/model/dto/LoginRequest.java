package nl.inholland.bank_api.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LoginRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Incorrect email format")
    public String email;

    @NotBlank(message = "Password is required")
    public String password;
}
