package nl.inholland.bank_api.model.dto;

public class LoginResponseDTO {
    public String token;

    public LoginResponseDTO(String token) {
        this.token = token;
    }
}
