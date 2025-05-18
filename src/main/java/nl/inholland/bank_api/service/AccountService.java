package nl.inholland.bank_api.service;

import jakarta.persistence.EntityNotFoundException;
import nl.inholland.bank_api.mapper.AccountMapper;
import nl.inholland.bank_api.model.dto.AccountDTO;
import nl.inholland.bank_api.model.dto.AccountWithUserDTO;
import nl.inholland.bank_api.model.dto.UpdateAccountLimitsDTO;
import nl.inholland.bank_api.model.entities.Account;
import nl.inholland.bank_api.model.enums.Operation;
import nl.inholland.bank_api.repository.AccountRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    public AccountService(AccountRepository accountRepository, AccountMapper accountMapper) {
        this.accountRepository = accountRepository;
        this.accountMapper = accountMapper;
    }

    public Account fetchAccountByIban(String iban) {
        return accountRepository.findByIban(iban.trim())
                .orElseThrow(() -> new EntityNotFoundException("Account not found for IBAN: " + iban));
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
        dto.setId(account.getId());
        dto.setIban(account.getIban());
        dto.setType(account.getType().name());
        dto.setBalance(account.getBalance());

        if (account.getUser() != null) {
            dto.setUserId(account.getUser().getId());
        }

        return dto;
    }

    public Page<AccountWithUserDTO> fetchAllAccounts(Pageable pageable) {
        Page<Account> accounts = accountRepository.findAll(pageable);
        return accounts.map(accountMapper::toAccountWithUserDTO);
    }

    public void updateAccountLimits(String iban, UpdateAccountLimitsDTO dto) {
        Account account = accountRepository.findByIban(iban)
                .orElseThrow(() -> new EntityNotFoundException("Account not found with iban: " + iban));

        account.setDailyLimit(dto.getDailyLimit());
        account.setAbsoluteLimit(dto.getAbsoluteLimit());
        account.setWithdrawLimit(dto.getWithdrawLimit());

        accountRepository.save(account);
    }

}