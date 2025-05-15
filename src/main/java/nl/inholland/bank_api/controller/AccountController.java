package nl.inholland.bank_api.controller;

import nl.inholland.bank_api.model.dto.AccountDTO;
import nl.inholland.bank_api.model.dto.AccountWithUserDTO;
import nl.inholland.bank_api.service.AccountService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
        AccountDTO account = accountService.fetchAccountDTOByIban(iban);
        return ResponseEntity.ok(account);
    }

    @GetMapping("/users/{userId}/accounts")
    public ResponseEntity<List<AccountDTO>> fetchAccountsByUserId(@PathVariable Long userId) {
        List<AccountDTO> accounts =  accountService.fetchAccountsByUserId(userId);
        return ResponseEntity.ok(accounts);
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/accounts")
    public ResponseEntity<List<AccountWithUserDTO>> fetchAllAccounts(@PageableDefault(size = 10) Pageable pageable) {
        List<AccountWithUserDTO> accounts = accountService.fetchAllAccounts(pageable).getContent();;
        return ResponseEntity.ok(accounts);
    }
}
