package nl.inholland.bank_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import nl.inholland.bank_api.model.dto.AccountWithUserDTO;
import nl.inholland.bank_api.model.dto.ApprovalStatusUpdateDTO;
import nl.inholland.bank_api.model.dto.ExceptionDTO;
import nl.inholland.bank_api.model.dto.UserProfileDTO;
import nl.inholland.bank_api.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(
            summary = "Get all pending users (EMPLOYEE only)",
            description = "Retrieves a list of users who are pending approval. Only accessible by employees."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Pending users retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = UserProfileDTO.class)),
                            examples = @ExampleObject(
                                    name = "Pending Users",
                                    summary = "Example list of pending users",
                                    value = """
                [
                  {
                    "id": 101,
                    "firstName": "Jane",
                    "lastName": "Doe",
                    "email": "jane.doe@example.com",
                    "status": "PENDING"
                  },
                  {
                    "id": 102,
                    "firstName": "Mark",
                    "lastName": "Smith",
                    "email": "mark.smith@example.com",
                    "status": "PENDING"
                  }
                ]
                """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized – JWT token is missing, invalid, expired, or malformed",
                    content = @Content(schema = @Schema(implementation = ExceptionDTO.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden – You do not have access to this resource",
                    content = @Content(schema = @Schema(implementation = ExceptionDTO.class))
            )
    })
    @GetMapping("/users/pending")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<List<UserProfileDTO>> getPendingUsers() {
        List<UserProfileDTO> pendingUsers = userService.getPendingUsers();
        return ResponseEntity.ok(pendingUsers);
    }

    @Operation(
            summary = "Get user profile by ID (EMPLOYEE only)",
            description = "Retrieves the profile of a user by their ID. Only accessible by employees."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User profile retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserProfileDTO.class),
                            examples = @ExampleObject(
                                    name = "User Profile",
                                    summary = "Example user profile",
                                    value = """
                {
                  "id": 123,
                  "firstName": "Alice",
                  "lastName": "Johnson",
                  "email": "alice.johnson@example.com",
                  "phoneNumber": "+31612345678",
                  "bsn": "123456789",
                  "status": "ACTIVE"
                }
                """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized – JWT token is missing, invalid, expired, or malformed",
                    content = @Content(schema = @Schema(implementation = ExceptionDTO.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden – You do not have access to this resource",
                    content = @Content(schema = @Schema(implementation = ExceptionDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionDTO.class),
                            examples = @ExampleObject(
                                    name = "User Not Found",
                                    summary = "User not found error",
                                    value = """
                {
                  "status": 404,
                  "exception": "EntityNotFoundException",
                  "message": ["User not found with id: 123"]
                }
                """
                            )
                    )
            )
    })
    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<UserProfileDTO> getUser(@PathVariable Long id) {
        UserProfileDTO userProfileDTO = userService.getProfileById(id);
                return ResponseEntity.ok(userProfileDTO);
    }

    @Operation(
            summary = "Get all active users (EMPLOYEE only)",
            description = "Retrieves a list of users who have at least one active account. Only accessible by employees."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Active users retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = UserProfileDTO.class)),
                            examples = @ExampleObject(
                                    name = "Active Users",
                                    summary = "Example list of active users",
                                    value = """
                [
                  {
                    "id": 201,
                    "firstName": "Emma",
                    "lastName": "Brown",
                    "email": "emma.brown@example.com",
                    "status": "APPROVED"
                  },
                  {
                    "id": 202,
                    "firstName": "Liam",
                    "lastName": "Wilson",
                    "email": "liam.wilson@example.com",
                    "status": "APPROVED"
                  }
                ]
                """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized – JWT token is missing, invalid, expired, or malformed",
                    content = @Content(schema = @Schema(implementation = ExceptionDTO.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden – You do not have access to this resource",
                    content = @Content(schema = @Schema(implementation = ExceptionDTO.class))
            )
    })
    @GetMapping("/users/accounts/active")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<List<UserProfileDTO>> getActiveUsers() {
        List<UserProfileDTO> userProfileDTO = userService.getActiveUsers();
        return ResponseEntity.ok(userProfileDTO);
    }

    @Operation(
            summary = "Update user approval status (EMPLOYEE only)",
            description = "Updates the approval status of a user by their ID. Only accessible by employees."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Approval status updated successfully – No content returned"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body or status value",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionDTO.class),
                            examples = @ExampleObject(
                                    name = "Invalid Status",
                                    summary = "Invalid approval status provided",
                                    value = """
                {
                  "status": 400,
                  "exception": "ValidationException",
                  "message": ["Invalid approval status: UNKNOWN"]
                }
                """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized – JWT token is missing, invalid, expired, or malformed",
                    content = @Content(schema = @Schema(implementation = ExceptionDTO.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden – You do not have access to this resource",
                    content = @Content(schema = @Schema(implementation = ExceptionDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionDTO.class),
                            examples = @ExampleObject(
                                    name = "User Not Found",
                                    summary = "User not found error",
                                    value = """
                {
                  "status": 404,
                  "exception": "EntityNotFoundException",
                  "message": ["User not found with id: 123"]
                }
                """
                            )
                    )
            )
    })
    @PutMapping("/users/{id}/approval-status")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<Void> updateApprovalStatus(
            @PathVariable Long id,
            @RequestBody ApprovalStatusUpdateDTO request
    ) {
        userService.updateApprovalStatus(id, request.getUserAccountStatus());
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Create default accounts for a user (EMPLOYEE only)",
            description = "Creates one or more default accounts for a user with the given ID. Only accessible by employees.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "List of accounts to be created for the user",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = AccountWithUserDTO.class)),
                            examples = @ExampleObject(
                                    name = "Default Accounts",
                                    summary = "Two default accounts for a user",
                                    value = """
                [
                  {
                    "iban": "NL91ABNA0417164300",
                    "type": "CHECKING",
                    "balance": 0,
                    "firstName": "Alice",
                    "lastName": "Smith",
                    "dailyLimit": 5000,
                    "absoluteLimit": -200,
                    "withdrawLimit": 3000
                  },
                  {
                    "iban": "NL91ABNA0417164301",
                    "type": "SAVINGS",
                    "balance": 0,
                    "firstName": "Alice",
                    "lastName": "Smith",
                    "dailyLimit": 3000,
                    "absoluteLimit": -100,
                    "withdrawLimit": 1500
                  }
                ]
                """
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Accounts created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body",
                    content = @Content(schema = @Schema(implementation = ExceptionDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized – JWT token is missing, invalid, expired, or malformed",
                    content = @Content(schema = @Schema(implementation = ExceptionDTO.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden – You do not have access to this resource",
                    content = @Content(schema = @Schema(implementation = ExceptionDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionDTO.class),
                            examples = @ExampleObject(
                                    name = "User Not Found",
                                    summary = "User not found error",
                                    value = """
                {
                  "status": 404,
                  "exception": "EntityNotFoundException",
                  "message": ["User not found with id: 123"]
                }
                """
                            )
                    )
            )
    })
    @PostMapping("/users/{id}/accounts")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<Void> createDefaultAccounts(
            @PathVariable Long id,
            @RequestBody List<AccountWithUserDTO> accounts
    ) {
        userService.createAccountsForUser(id, accounts);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(
            summary = "Close user and their accounts (EMPLOYEE only)",
            description = "Closes the user and all associated accounts by user ID. Only accessible by employees."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "User and accounts closed successfully – No content returned"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized – JWT token is missing, invalid, expired, or malformed",
                    content = @Content(schema = @Schema(implementation = ExceptionDTO.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden – You do not have access to this resource",
                    content = @Content(schema = @Schema(implementation = ExceptionDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionDTO.class),
                            examples = @ExampleObject(
                                    name = "User Not Found",
                                    summary = "User not found error",
                                    value = """
                {
                  "status": 404,
                  "exception": "EntityNotFoundException",
                  "message": ["User not found with id: 123"]
                }
                """
                            )
                    )
            )
    })
    @PutMapping("/users/{id}/close")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<Void> closeUserWithAccounts(@PathVariable Long id) {
        userService.closeUserAndAccounts(id);
        return ResponseEntity.noContent().build();
    }
}
