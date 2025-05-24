package nl.inholland.bank_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(
            summary = "Register a new user",
            description = """
                    This endpoint registers a new customer in the system.
                    The input must include personal and login details like email, name, BSN, phone number, and password.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "User successfully registered",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RegisterResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid registration data",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionDTO.class)
                    )
            )
    })
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
