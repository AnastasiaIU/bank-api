package nl.inholland.bank_api.controller;

import jakarta.validation.Valid;
import nl.inholland.bank_api.model.dto.LoginRequest;
import nl.inholland.bank_api.model.dto.LoginResponse;
import nl.inholland.bank_api.model.dto.ResponseIdDTO;
import nl.inholland.bank_api.model.dto.UserDTO;
import nl.inholland.bank_api.model.entities.User;
import nl.inholland.bank_api.service.UserService;
import nl.inholland.bank_api.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.stream.Collectors;


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
    public ResponseEntity<ResponseIdDTO> register(@Valid @RequestBody UserDTO dto) {
        Long id = userService.add(dto);
        return ResponseEntity.status(201).body(new ResponseIdDTO(id));
    }

    @PostMapping("login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email, request.password)
            );

            User user = userService.findByEmail(request.email.trim());

            if (!user.isApproved()) {
                return ResponseEntity.status(403).body("Can't login, you are not approved yet.");
            }

            String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
            UserDTO userDTO = userService.toUserDTO(user);

            return ResponseEntity.ok(new LoginResponse(token, userDTO));
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid username or password");
        }
    }

    @GetMapping("me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        String email = authentication.getName(); // comes from JWT
        User user = userService.findByEmail(email);

        UserDTO userDTO = userService.toUserDTO(user);

        return ResponseEntity.ok(userDTO);
    }
}
