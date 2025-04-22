package nl.inholland.bank_api.controller;

import nl.inholland.bank_api.model.dto.AccountDTO;
import nl.inholland.bank_api.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/accounts/{iban}")
    public ResponseEntity<AccountDTO> fetchAccountByIban(@PathVariable String iban) {
        AccountDTO account = accountService.fetchAccountByIban(iban);
        return ResponseEntity.ok(account);
    }

    @GetMapping("/users/{userId}/accounts")
    public ResponseEntity<List<AccountDTO>> fetchAccountsByUserId(@PathVariable Long userId) {
        List<AccountDTO> accounts =  accountService.fetchAccountsByUserId(userId);
        return ResponseEntity.ok(accounts);
    }
}
