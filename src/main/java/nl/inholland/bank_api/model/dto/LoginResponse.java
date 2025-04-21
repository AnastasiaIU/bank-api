package nl.inholland.bank_api.model.dto;

public class LoginResponse {
    public String token;

    public LoginResponse(String token) {
        this.token = token;
    }
}
