package nl.inholland.bank_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import nl.inholland.bank_api.model.dto.AccountDTO;
import nl.inholland.bank_api.model.dto.AccountWithUserDTO;
import nl.inholland.bank_api.model.dto.ExceptionDTO;
import nl.inholland.bank_api.model.dto.UpdateAccountLimitsDTO;
import nl.inholland.bank_api.service.AccountService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/accounts/{iban}")
    public ResponseEntity<AccountDTO> fetchAccountByIban(@PathVariable String iban) {
        AccountDTO account = accountService.fetchAccountDTOByIban(iban);
        return ResponseEntity.ok(account);
    }

    @GetMapping("/users/{userId}/accounts")
    public ResponseEntity<List<AccountDTO>> fetchAccountsByUserId(@PathVariable Long userId) {
        List<AccountDTO> accounts =  accountService.fetchAccountsByUserId(userId);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/users/{userId}/checking-accounts")
    public ResponseEntity<List<AccountDTO>> fetchCheckingAccountsByUserId(@PathVariable Long userId) {
        List<AccountDTO> accounts =  accountService.fetchCheckingAccountsByUserId(userId);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/users/accounts/{firstName}/{lastName}")
    public ResponseEntity<List<AccountDTO>> fetchAccountsByName(@PathVariable String firstName, @PathVariable String lastName) {
        List<AccountDTO> accounts = accountService.fetchAccountsByName(firstName, lastName);
        return ResponseEntity.ok(accounts);
    }

    @Operation(
            summary = "Get all customer accounts (EMPLOYEE only)",
            description = "Retrieve a paginated list of all accounts including user information. Accessible only to employees."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Accounts retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AccountWithUserDTO.class),
                            examples = @ExampleObject(
                                    name = "Paginated Accounts Response",
                                    summary = "Paginated list of accounts with user info",
                                    value = """
                    {
                      "content": [
                        {
                          "iban": "NL91ABNA0417164300",
                          "type": "CHECKING",
                          "balance": 10000,
                          "firstName": "John",
                          "lastName": "Doe",
                          "dailyLimit": 5000,
                          "absoluteLimit": -300,
                          "withdrawLimit": 3000
                        },
                        {
                          "iban": "NL91ABNA0417164301",
                          "type": "SAVINGS",
                          "balance": 50000,
                          "firstName": "John",
                          "lastName": "Doe",
                          "dailyLimit": 5000,
                          "absoluteLimit": -100,
                          "withdrawLimit": 1000
                        }
                      ],
                      "pageable": {
                        "pageNumber": 0,
                        "pageSize": 10,
                        "sort": {
                          "empty": true,
                          "sorted": false,
                          "unsorted": true
                        },
                        "offset": 0,
                        "unpaged": false,
                        "paged": true
                      },
                      "last": true,
                      "totalPages": 1,
                      "totalElements": 2,
                      "size": 10,
                      "number": 0,
                      "sort": {
                        "empty": true,
                        "sorted": false,
                        "unsorted": true
                      },
                      "first": true,
                      "numberOfElements": 2,
                      "empty": false
                    }
                """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden – You do not have access to this resource",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Access Denied",
                                            summary = "User lacks required authorization",
                                            value = """
                        {
                          "status": 403,
                          "exception": "AuthorizationDeniedException",
                          "message": ["Access denied"]
                        }
                    """
                                    )
                            }
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

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/accounts")
    public ResponseEntity<Page<AccountWithUserDTO>> fetchAllAccounts(@PageableDefault(size = 10, page = 0) Pageable pageable) {
        Page<AccountWithUserDTO> accounts = accountService.fetchAllAccounts(pageable);
        return ResponseEntity.ok(accounts);
    }

    @Operation(
            summary = "Update a customer account's limits (EMPLOYEE only)",
            description = "Update the daily, absolute, and withdraw limits for a specific account by IBAN. Requires employee authorization.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "New limits for the account",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UpdateAccountLimitsDTO.class)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Account limits updated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body or validation failed",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionDTO.class)
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
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden – You do not have access to this resource",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Access Denied",
                                            summary = "User lacks required authorization",
                                            value = """
                        {
                          "status": 403,
                          "exception": "AuthorizationDeniedException",
                          "message": ["Access denied"]
                        }
                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Account not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionDTO.class),
                            examples = @ExampleObject(
                                    name = "Account Not Found",
                                    summary = "Account not found error",
                                    value = """
                    {
                      "status": 404,
                      "exception": "EntityNotFoundException",
                      "message": [
                        "Account not found with iban: NL91ABNA0417161200"
                      ]
                    }
                """
                            )
                    )
            )
    })
    @PreAuthorize("hasRole('EMPLOYEE')")
    @PutMapping("/accounts/{iban}/limits")
    public ResponseEntity<?> updateAccountLimits(@PathVariable String iban,@Valid @RequestBody UpdateAccountLimitsDTO dto) {
        accountService.updateAccountLimits(iban, dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/users/{id}/accounts/review")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<List<AccountWithUserDTO>> createDefaultAccounts(@PathVariable Long id) {
        List<AccountWithUserDTO> accounts = accountService.createAccountsByUserId(id);
        return ResponseEntity.ok(accounts);
    }

    @DeleteMapping("/accounts/{iban}/close")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<Void> closeAccount(@PathVariable String iban) {
        accountService.closeAccountByIban(iban);
        return ResponseEntity.noContent().build();
    }
}