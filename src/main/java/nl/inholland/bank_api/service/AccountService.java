package nl.inholland.bank_api.service;

import nl.inholland.bank_api.model.dto.AccountDTO;
import nl.inholland.bank_api.model.entities.Account;
import nl.inholland.bank_api.repository.AccountRepository;
import org.springframework.stereotype.Service;

@Service
public class AccountService {
    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public AccountDTO fetchAccountByIban(String iban) {
        Account account = accountRepository.findByIban(iban).orElseThrow(() -> new IllegalArgumentException("Account not found with IBAN: " + iban));
        return toDTO(account);
    }

    public Account fetchAccountById(Long id) {
        return accountRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Account not found with ID: " + id));
    }

    private AccountDTO toDTO(Account account) {
        AccountDTO dto = new AccountDTO();
        dto.setIban(account.getIban());
        dto.setType(account.getType().name());
        dto.setBalance(account.getBalance());

        if (account.getUser() != null) {
            dto.setUserId(account.getUser().getId());
        }

        return dto;
    }
}