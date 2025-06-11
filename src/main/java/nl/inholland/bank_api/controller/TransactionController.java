package nl.inholland.bank_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import nl.inholland.bank_api.model.dto.ExceptionDTO;
import nl.inholland.bank_api.model.dto.TransactionRequestDTO;
import nl.inholland.bank_api.service.TransactionService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Collections;

@RestController
@RequestMapping("transactions")
@Validated
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Operation(
            summary = "Post a new transaction",
            description = "Initiate a new bank transfer between two accounts. Requires a valid source and target IBAN, amount, and user initiating the transaction.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TransactionRequestDTO.class),
                            examples = @ExampleObject(
                                    name = "Sample Transaction Request",
                                    summary = "Valid transaction request",
                                    value = """
                                            {
                                              "sourceAccount": "NL91ABNA0417164300",
                                              "targetAccount": "NL91ABNA0417164301",
                                              "initiatedBy": 1,
                                              "amount": 250.00,
                                              "description": "Monthly savings"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Transaction created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Transaction Created",
                                    summary = "Transaction ID response",
                                    value = """
                                            {
                                              "id": 42
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request – Validation or business logic error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionDTO.class),
                            examples = @ExampleObject(
                                    name = "Validation Error",
                                    summary = "Missing or invalid fields",
                                    value = """
                                            {
                                              "status": 400,
                                              "exception": "ValidationException",
                                              "message": ["Source account must not be empty", "Amount must be greater than 0"]
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized – JWT token missing or invalid",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionDTO.class),
                            examples = @ExampleObject(
                                    name = "Unauthorized Access",
                                    summary = "JWT token issues",
                                    value = """
                                            {
                                              "status": 401,
                                              "exception": "JwtException",
                                              "message": ["Missing token or Authorization header"]
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden – Access denied for current role",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionDTO.class),
                            examples = @ExampleObject(
                                    name = "Forbidden",
                                    summary = "User lacks permission",
                                    value = """
                                            {
                                              "status": 403,
                                              "exception": "AuthorizationDeniedException",
                                              "message": ["Access denied"]
                                            }
                                            """
                            )
                    )
            )
    })

    @PostMapping()
    public ResponseEntity<?> postTransaction(@Valid @RequestBody TransactionRequestDTO dto) {
        Long id = transactionService.postTransaction(dto);
        return ResponseEntity.status(201).body(Collections.singletonMap("id", id));
    }
}