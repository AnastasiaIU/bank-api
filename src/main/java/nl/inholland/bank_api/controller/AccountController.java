package nl.inholland.bank_api.controller;

import nl.inholland.bank_api.model.dto.AccountDTO;
import nl.inholland.bank_api.service.AccountService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("accounts")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/{iban}")
    public AccountDTO fetchAccountByIban(@PathVariable String iban) {
        return accountService.fetchAccountByIban(iban);
    }
}
