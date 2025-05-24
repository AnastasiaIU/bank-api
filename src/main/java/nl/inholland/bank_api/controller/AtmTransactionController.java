package nl.inholland.bank_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import nl.inholland.bank_api.model.dto.AtmTransactionDTO;
import nl.inholland.bank_api.model.dto.AtmTransactionRequestDTO;
import nl.inholland.bank_api.model.dto.ExceptionDTO;
import nl.inholland.bank_api.model.entities.Account;
import nl.inholland.bank_api.model.entities.User;
import nl.inholland.bank_api.service.AccountService;
import nl.inholland.bank_api.service.AtmTransactionService;
import nl.inholland.bank_api.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("atm")
public class AtmTransactionController {
    private final AtmTransactionService atmTransactionService;
    private final AccountService accountService;
    private final UserService userService;

    public AtmTransactionController(
            AtmTransactionService atmTransactionService,
            AccountService accountService,
            UserService userService
    ) {
        this.atmTransactionService = atmTransactionService;
        this.accountService = accountService;
        this.userService = userService;
    }

    @Operation(
            summary = "Create a new ATM transaction",
            description = """
                        This endpoint allows a customer to initiate an ATM transaction on one of their accounts.
                        The transaction can be a deposit (adding funds) or a withdrawal (removing funds).
                        The transaction will be marked as PENDING initially and processed asynchronously by the system.
                        The authenticated user must be the owner of the account.
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "ATM transaction details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AtmTransactionRequestDTO.class)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "ATM transaction was successfully created",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AtmTransactionDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid transaction data",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized – JWT token is missing or invalid",
                    content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden – You do not have access to this transaction",
                    content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found or account not found for IBAN",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionDTO.class)
                    )
            )
    })
    @PostMapping("transactions")
    public ResponseEntity<AtmTransactionDTO> createTransaction(
            @Valid @RequestBody AtmTransactionRequestDTO dto,
            Authentication authentication
    ) {
        String email = authentication.getName();
        User currentUser = userService.getUserByEmail(email);
        Account account = accountService.fetchAccountByIban(dto.iban);

        AtmTransactionDTO createdTransaction = atmTransactionService.createTransaction(dto, account, currentUser);
        return ResponseEntity.status(201).body(createdTransaction);
    }

    @Operation(
            summary = "Get ATM transaction by ID",
            description = """
                    This endpoint retrieves the details of a specific ATM transaction
                    by its unique ID. The authenticated user must be the owner of
                    the account associated with the transaction.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Transaction found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AtmTransactionDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized – JWT token is missing or invalid",
                    content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden – You do not have access to this transaction",
                    content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Transaction not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionDTO.class)
                    )
            )
    })
    @GetMapping("transactions/{id}")
    public ResponseEntity<AtmTransactionDTO> getTransaction(@PathVariable Long id) {
        AtmTransactionDTO transaction = atmTransactionService.getTransaction(id);
        return ResponseEntity.ok(transaction);
    }
}
