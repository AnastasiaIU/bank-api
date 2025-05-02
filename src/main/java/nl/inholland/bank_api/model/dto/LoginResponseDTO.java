package nl.inholland.bank_api.model.dto;

public class LoginResponseDTO {
    public String token;
    public UserDTO user;

    public LoginResponseDTO(String token, UserDTO user) {
        this.token = token;
        this.user = user;
    }
}
