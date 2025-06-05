package nl.inholland.bank_api.service;

import jakarta.persistence.EntityNotFoundException;
import nl.inholland.bank_api.mapper.AccountMapper;
import nl.inholland.bank_api.model.dto.AccountDTO;
import nl.inholland.bank_api.model.dto.AccountWithUserDTO;
import nl.inholland.bank_api.model.dto.UpdateAccountLimitsDTO;
import nl.inholland.bank_api.model.entities.Account;
import nl.inholland.bank_api.model.entities.User;
import nl.inholland.bank_api.model.enums.AccountStatus;
import nl.inholland.bank_api.model.enums.AccountType;
import nl.inholland.bank_api.model.enums.Operation;
import nl.inholland.bank_api.repository.AccountRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private static final String BANK_CODE = "INHO0";
    private static final String COUNTRY_CODE = "NL";
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

    public List<AccountDTO> fetchAccountsByName(String firstName, String lastName) {
        List<Account> accounts = accountRepository.findByFirstNameAndLastName(firstName, lastName);
        return accounts.stream()
                .filter(account -> AccountType.CHECKING.equals(account.getType()))
                .map(this::toDTO)
                .toList();
    }

    public List<AccountDTO> fetchAccountsByUserId(Long userId) {
        List<Account> accounts = accountRepository.findByUserId(userId);

        return accounts.stream()
                .map(this::toDTO)
                .toList();
    }

    public List<AccountDTO> fetchCheckingAccountsByUserId(Long userId) {
        List<Account> accounts = accountRepository.findByUserId(userId);
        return  accounts.stream()
                .filter(account -> AccountType.CHECKING.equals(account.getType()))
                .map(this::toDTO)
                .toList();

    }

    private AccountDTO toDTO(Account account) {
        AccountDTO dto = new AccountDTO();
        dto.setId(account.getId());
        dto.setIban(account.getIban());
        dto.setStatus(account.getStatus().name());
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


    public List<AccountWithUserDTO> createAccountsByUserId(Long userId) {
        List<Account> accounts = List.of(
                createAccountForUser(userId, AccountType.CHECKING),
                createAccountForUser(userId, AccountType.SAVINGS)
        );

        return accounts.stream()
                .map(accountMapper::toAccountWithUserDTO)
                .collect(Collectors.toList());
    }

    private Account createAccountForUser(Long id, AccountType type) {
        Account account = new Account();
        account.setUser(new User());
        account.setStatus(AccountStatus.ACTIVE);
        account.setType(type);
        account.setBalance(BigDecimal.ZERO);
        account.setIban(generateUniqueIbanSafely());
        account.setAbsoluteLimit(BigDecimal.ZERO);
        account.setWithdrawLimit(BigDecimal.ZERO);
        account.setDailyLimit(BigDecimal.ZERO);
         return account;
    }

    private String generateUniqueIbanSafely() {
        String iban;
        do {
            iban = generateRawIban();
        } while (accountRepository.existsByIban(iban));
        return iban;
    }

    private String generateRawIban() {
        String checkDigits = String.format("%02d", new Random().nextInt(100));
        String randomDigits = String.format("%09d", new Random().nextInt(1_000_000_000));
        return COUNTRY_CODE + checkDigits + BANK_CODE + randomDigits;
    }

    public void saveAccounts(User user, List<AccountWithUserDTO> accountDTOs) {
        List<Account> accounts = accountDTOs.stream()
                .map(dto -> accountMapper.toAccount(dto, user))
                .toList();

        accountRepository.saveAll(accounts);
    }

    public void closeAccountByIban(String iban) {
        Account account = accountRepository.findByIban(iban)
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));

        account.setStatus(AccountStatus.CLOSED);
        accountRepository.save(account);
    }

    public void closeAllAccountsForUser(Long id) {
        List<Account> accounts = accountRepository.findByUserId(id);
        for (Account account : accounts) {
            account.setStatus(AccountStatus.CLOSED);
        }
        accountRepository.saveAll(accounts);
    }
}