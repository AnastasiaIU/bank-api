package nl.inholland.bank_api.service;

import nl.inholland.bank_api.model.dto.AccountDTO;
import nl.inholland.bank_api.model.entities.Account;
import nl.inholland.bank_api.model.entities.User;
import nl.inholland.bank_api.model.enums.AccountType;
import nl.inholland.bank_api.repository.AccountRepository;
import nl.inholland.bank_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountService {
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    @Autowired
    public AccountService(UserRepository userRepository, AccountRepository accountRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
    }

    public AccountDTO fetchAccount(String iban) {
        Account account = accountRepository.findByIban(iban).orElseThrow(() -> new IllegalArgumentException("Account not found with IBAN: " + iban));
        return toDTO(account);
    }

    private Account toAccount(AccountDTO dto) {
        Account account = new Account();

        User user = userRepository.findById(dto.userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + dto.userId));

        account.setUser(user);
        account.setIban(dto.iban);
        account.setType(AccountType.valueOf(dto.type));
        account.setBalance(dto.balance);

        return account;
    }

    public AccountDTO toDTO(Account account) {
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