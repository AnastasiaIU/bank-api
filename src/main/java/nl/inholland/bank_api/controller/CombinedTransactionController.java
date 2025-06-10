package nl.inholland.bank_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import nl.inholland.bank_api.constant.ErrorMessages;
import nl.inholland.bank_api.model.dto.CombinedTransactionDTO;
import nl.inholland.bank_api.model.dto.CombinedTransactionFullHistoryDTO;
import nl.inholland.bank_api.model.dto.ExceptionDTO;
import nl.inholland.bank_api.model.dto.TransactionFilterDTO;
import nl.inholland.bank_api.model.entities.User;
import nl.inholland.bank_api.model.enums.UserRole;
import nl.inholland.bank_api.service.AccountService;
import nl.inholland.bank_api.service.CombinedTransactionService;
import nl.inholland.bank_api.service.UserService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
public class CombinedTransactionController {
    private final CombinedTransactionService combinedTransactionService;

    private final UserService userService;

    private final AccountService accountService;

    public CombinedTransactionController(CombinedTransactionService combinedTransactionService, UserService userService, AccountService accountService) {
        this.combinedTransactionService = combinedTransactionService;
        this.userService = userService;
        this.accountService = accountService;
    }

    @GetMapping("combined-transactions")
    public Page<CombinedTransactionFullHistoryDTO> getAllCombinedTransactions(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return combinedTransactionService.getAllCombinedTransactions(pageable);
    }

    @Operation(
            summary = "Get all combined transactions (ATM + Transfers) for a specific account",
            description = """
        This endpoint retrieves a paginated and filtered list of all transactions 
        (ATM and transfer transactions) associated with the given account ID.

        The authenticated user must either:
        - Own the account, or
        - Have an EMPLOYEE role.

        Filters can be applied via query parameters:
        • startDate / endDate (yyyy-MM-dd)  
        • amount + comparison (lt / eq / gt)  
        • sourceIban / targetIban  
        • description (partial match)

        Pagination and sorting are also supported using standard Pageable parameters.
    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Filtered list of combined transactions returned successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = CombinedTransactionDTO.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized – JWT token is missing or invalid",
                    content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden – You do not have access to this account",
                    content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Account not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionDTO.class)
                    )
            )
    })
    @GetMapping("/accounts/{accountId}/transactions")
    public ResponseEntity<Page<CombinedTransactionDTO>> getAllAccountTransactions(
            @PathVariable Long accountId, @ModelAttribute TransactionFilterDTO transactionFilterDTO, @ParameterObject Pageable pageable, Authentication authentication)
    {
        String email = authentication.getName();
        User currentUser = userService.getUserByEmail(email);
        boolean isEmployee = UserRole.EMPLOYEE.equals(currentUser.getRole());
        boolean ownsAccount = accountService.ownsAccount(currentUser.getId(), accountId);

        if (!ownsAccount && !isEmployee) {
            throw new AccessDeniedException(ErrorMessages.ACCESS_DENIED);
        }
        Page<CombinedTransactionDTO> transactions = combinedTransactionService.getFilteredTransactions(
                accountId, transactionFilterDTO, pageable);

        return ResponseEntity.ok(transactions);
    }
}