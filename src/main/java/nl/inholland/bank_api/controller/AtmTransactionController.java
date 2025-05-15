package nl.inholland.bank_api.controller;

import jakarta.validation.Valid;
import nl.inholland.bank_api.model.dto.AtmTransactionDTO;
import nl.inholland.bank_api.model.dto.AtmTransactionRequestDTO;
import nl.inholland.bank_api.model.entities.Account;
import nl.inholland.bank_api.model.entities.User;
import nl.inholland.bank_api.model.enums.UserRole;
import nl.inholland.bank_api.service.AccountService;
import nl.inholland.bank_api.service.AtmTransactionService;
import nl.inholland.bank_api.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
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

    @PostMapping("transactions")
    public ResponseEntity<AtmTransactionDTO> createTransaction(
            @Valid @RequestBody AtmTransactionRequestDTO dto,
            Authentication authentication
    ) {
        String email = authentication.getName();
        User currentUser = userService.getUserByEmail(email);
        Account account = accountService.fetchAccountByIban(dto.iban);

        boolean isCustomer = UserRole.CUSTOMER.equals(currentUser.getRole());

        if (!isCustomer && !currentUser.getId().equals(account.getUser().getId())) {
            throw new AccessDeniedException("You are not allowed to perform this action.");
        }

        AtmTransactionDTO createdTransaction = atmTransactionService.createTransaction(dto, account, currentUser);
        return ResponseEntity.status(201).body(createdTransaction);
    }
}
