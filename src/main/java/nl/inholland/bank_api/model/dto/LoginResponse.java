package nl.inholland.bank_api.model.dto;

public class LoginResponse {
    public String token;
    public UserDTO user;

    public LoginResponse(String token, UserDTO user) {
        this.token = token;
        this.user = user;
    }
}
