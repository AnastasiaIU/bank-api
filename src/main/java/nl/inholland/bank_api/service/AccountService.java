package nl.inholland.bank_api.service;

import nl.inholland.bank_api.model.dto.AccountDTO;
import nl.inholland.bank_api.model.entities.Account;
import nl.inholland.bank_api.model.enums.Operation;
import nl.inholland.bank_api.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AccountService {
    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account fetchAccountByIban(String iban) {
        return accountRepository.findByIban(iban).orElse(null);
    }

    public AccountDTO fetchAccountDTOByIban(String iban) {
        return toDTO(fetchAccountByIban(iban));
    }

    public void updateBalance(Account account, BigDecimal amount, Operation operation) {
        if (operation == Operation.ADDITION) {
            account.setBalance(account.getBalance().add(amount));
        } else if (operation == Operation.SUBTRACTION) {
            account.setBalance(account.getBalance().subtract(amount));
        }

        accountRepository.save(account);
    }

    public List<AccountDTO> fetchAccountsByUserId(Long userId) {
        List<Account> accounts = accountRepository.findByUserId(userId);

        return accounts.stream()
                .map(this::toDTO)
                .toList();
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