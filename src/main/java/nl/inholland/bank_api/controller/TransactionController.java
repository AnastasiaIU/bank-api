package nl.inholland.bank_api.controller;

import jakarta.validation.Valid;
import nl.inholland.bank_api.model.dto.CombinedTransactionDTO;
<<<<<<< HEAD
import nl.inholland.bank_api.model.dto.TransactionFilterDTO;
=======
>>>>>>> 0d1f036e6cb40cfb18e0f8e2849b6181203a46db
import nl.inholland.bank_api.model.dto.TransactionRequestDTO;
import nl.inholland.bank_api.model.entities.User;
import nl.inholland.bank_api.service.AccountService;
import nl.inholland.bank_api.service.TransactionService;
import nl.inholland.bank_api.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping
public class TransactionController {
    private final TransactionService transactionService;
    private final UserService userService;
    private final AccountService accountService;

    public TransactionController(TransactionService transactionService, UserService userService, AccountService accountService) {
        this.transactionService = transactionService;
        this.userService = userService;
        this.accountService = accountService;
    }

    @PostMapping("/transactions")
    public ResponseEntity<?> postTransaction(@Valid @RequestBody TransactionRequestDTO dto) {
        Long id = transactionService.postTransaction(dto);
        return ResponseEntity.status(201).body(Collections.singletonMap("id", id));
    }

    @GetMapping("/accounts/{accountId}/transactions")
    public ResponseEntity<List<CombinedTransactionDTO>> getAllAccountTransactions(
<<<<<<< HEAD
            @PathVariable Long accountId, @ModelAttribute TransactionFilterDTO transactionFilterDTO, Authentication authentication)
    {
        String email = authentication.getName();
        User currentUser = userService.getUserByEmail(email);

        boolean ownsAccount = accountService
                .fetchAccountsByUserId(currentUser.getId())
                .stream()
                .anyMatch(account -> account.getId().equals(accountId));
        if (!ownsAccount) {
            throw new AccessDeniedException("You are not authorized to view these transactions.");
        }
        System.out.println("FilterDTO received: " + transactionFilterDTO);
        List<CombinedTransactionDTO> transactions = transactionService.getFilteredTransactions(
                accountId, transactionFilterDTO);
=======
            @PathVariable Long accountId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) BigDecimal amount,
            @RequestParam(required = false) String comparison,
            @RequestParam(required = false) String sourceIban,
            @RequestParam(required = false) String targetIban
    ) {
        List<CombinedTransactionDTO> transactions = transactionService.getFilteredTransactions(
                accountId, startDate, endDate, amount, comparison, sourceIban, targetIban
        );
>>>>>>> 0d1f036e6cb40cfb18e0f8e2849b6181203a46db
        return ResponseEntity.ok(transactions);
    }
}