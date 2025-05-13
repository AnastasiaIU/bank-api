package nl.inholland.bank_api.controller;

import nl.inholland.bank_api.model.dto.AccountDTO;
import nl.inholland.bank_api.service.AccountService;
import nl.inholland.bank_api.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class AccountController {
    private final AccountService accountService;
    private final UserService userService;

    public AccountController(AccountService accountService, UserService userService) {
        this.accountService = accountService;
        this.userService = userService;
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

    @GetMapping("/users/accounts")
    public ResponseEntity<List<AccountDTO>> fetchAccountsByAccountId(@RequestHeader("Authorization") String authorizationHeader) {
        Long userId = userService.getUserIdFromToken(authorizationHeader);
        List<AccountDTO> accounts =  accountService.fetchAccountsByUserId(userId);
        return ResponseEntity.ok(accounts);
    }
}