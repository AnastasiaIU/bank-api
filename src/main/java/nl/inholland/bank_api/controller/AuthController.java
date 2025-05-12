package nl.inholland.bank_api.controller;

import jakarta.validation.Valid;
import nl.inholland.bank_api.model.dto.*;
import nl.inholland.bank_api.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("auth")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("register")
    public ResponseEntity<RegisterResponseDTO> register(@Valid @RequestBody RegisterRequestDTO registerRequestDTO) {
        Long id = userService.createUser(registerRequestDTO);
        return ResponseEntity.status(201).body(new RegisterResponseDTO(id));
    }

    @PostMapping("login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        LoginResponseDTO response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("me")
    public ResponseEntity<UserProfileDTO> getCurrentUser(Authentication authentication) {
        String email = authentication.getName(); // comes from JWT
        UserProfileDTO userProfileDTO = userService.getProfileByEmail(email);
        return ResponseEntity.ok(userProfileDTO);
    }
}
