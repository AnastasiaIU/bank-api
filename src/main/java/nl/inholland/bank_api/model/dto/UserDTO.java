package nl.inholland.bank_api.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class UserDTO {
    @NotBlank(message = "First name is required.")
    public String firstName;

    @NotBlank(message = "Last name is required.")
    public String lastName;

    @Email(message = "Invalid email format.")
    @NotBlank(message = "Email is required.")
    public String email;

    @NotBlank(message = "Password is required.")
    public String password;

    @NotBlank(message = "BSN is required.")
    @Pattern(regexp = "\\d{9}", message = "BSN must be exactly 9 digits.")
    public String bsn;

    @NotBlank(message = "Phone number is required.")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone number must be 10–15 digits (optionally starts with +).")
    public String phoneNumber;

    public boolean isApproved;
    public String role;
}
