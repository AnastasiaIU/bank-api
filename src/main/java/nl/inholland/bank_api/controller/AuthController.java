package nl.inholland.bank_api.controller;

import jakarta.validation.Valid;
import nl.inholland.bank_api.model.dto.LoginRequest;
import nl.inholland.bank_api.model.dto.LoginResponse;
import nl.inholland.bank_api.model.dto.UserDTO;
import nl.inholland.bank_api.model.entities.User;
import nl.inholland.bank_api.service.UserService;
import nl.inholland.bank_api.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Collections;

@RestController
@RequestMapping("auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    public AuthController(UserService userService, AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("register")
    public ResponseEntity<?> register(@Valid @RequestBody UserDTO dto) {
        Long id = userService.add(dto);
        return ResponseEntity.status(201).body(Collections.singletonMap("id", id));
    }

    @PostMapping("login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email, request.password)
            );

            User user = userService.findByEmail(request.email.trim());
            String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

            return ResponseEntity.ok(new LoginResponse(token));
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid username or password");
        }
    }

    @GetMapping("me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        String email = authentication.getName(); // comes from JWT
        User user = userService.findByEmail(email);

        UserDTO userDTO = userService.toUserDTO(user);

        return ResponseEntity.status(201).body(userDTO);
    }
}
