package nl.inholland.bank_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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

    @Operation(
            summary = "User login",
            description = "Authenticate a user with email and password to receive a JWT token.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "User credentials for login",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginRequestDTO.class)
                    )
            )
    )

    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful, JWT token returned",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed: missing or invalid email/password",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionDTO.class),
                            examples = {
                                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            name = "Missing both Fields",
                                            summary = "Missing email and password",
                                            value = """
{
  "status": 400,
  "exception": "MethodArgumentNotValidException",
  "message": [
    "password: Password is required",
    "email: Email is required"
  ]
}
"""
                                    ),
                                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            name = "Invalid Email Format",
                                            summary = "Incorrect email format with missing password",
                                            value = """
{
  "status": 400,
  "exception": "MethodArgumentNotValidException",
  "message": [
    "email: Incorrect email format",
    "password: Password is required"
  ]
}
"""
                                    ),
                                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            name = "Invalid Email Only",
                                            summary = "Incorrect email format only",
                                            value = """
{
  "status": 400,
  "exception": "MethodArgumentNotValidException",
  "message": [
    "email: Incorrect email format"
  ]
}
"""
                                    ),
                                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            name = "Missing Password Only",
                                            summary = "Missing password only",
                                            value = """
{
  "status": 400,
  "exception": "MethodArgumentNotValidException",
  "message": [
    "password: Password is required"
  ]
}
"""
                                    ),
                                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            name = "Missing Email Only",
                                            summary = "Missing email only",
                                            value = """
{
  "status": 400,
  "exception": "MethodArgumentNotValidException",
  "message": [
    "email: Email is required"
  ]
}
"""
                                    )
                            }
                    )
            ),

            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - invalid email or password",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionDTO.class),
                            examples = {
                                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            name = "Bad Credentials",
                                            summary = "Invalid email or password",
                                            value = """
                    {
                      "status": 401,
                      "exception": "BadCredentialsException",
                      "message": [
                        "Invalid email or password"
                      ]
                    }
                    """
                                    )
                            }
                    )
            )
    })

    @PostMapping("login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        LoginResponseDTO response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get current user",
            description = "Returns the details of the currently authenticated user, based on the JWT token."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User profile retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserProfileDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized – JWT token is missing, invalid, expired, or malformed",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Expired Token",
                                            summary = "JWT token is expired",
                                            value = """
                    {
                      "status": 401,
                      "exception": "ExpiredJwtException",
                      "message": ["Expired Token"]
                    }
                    """
                                    ),
                                    @ExampleObject(
                                            name = "Missing Token",
                                            summary = "JWT token or Authorization header is missing",
                                            value = """
                    {
                      "status": 401,
                      "exception": "JwtException",
                      "message": ["Missing token or Authorization header"]
                    }
                    """
                                    ),
                                    @ExampleObject(
                                            name = "Malformed Token",
                                            summary = "JWT token is malformed",
                                            value = """
                    {
                      "status": 401,
                      "exception": "MalformedJwtException",
                      "message": ["Malformed token"]
                    }
                    """
                                    )
                            }
                    )
            )
    })
    @GetMapping("me")
    public ResponseEntity<UserProfileDTO> getCurrentUser(Authentication authentication) {
        String email = authentication.getName(); // comes from JWT
        UserProfileDTO userProfileDTO = userService.getProfileByEmail(email);
        return ResponseEntity.ok(userProfileDTO);
    }
}
