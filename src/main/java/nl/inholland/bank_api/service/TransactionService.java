package nl.inholland.bank_api.service;

import nl.inholland.bank_api.mapper.AtmTransactionMapper;
import nl.inholland.bank_api.mapper.TransactionMapper;
import nl.inholland.bank_api.model.dto.CombinedTransactionDTO;
import nl.inholland.bank_api.model.dto.TransactionFilterDTO;
import nl.inholland.bank_api.model.dto.TransactionRequestDTO;
import nl.inholland.bank_api.model.entities.Account;
import nl.inholland.bank_api.model.entities.Transaction;
import nl.inholland.bank_api.model.enums.Operation;
import nl.inholland.bank_api.model.enums.Status;
import nl.inholland.bank_api.repository.AtmTransactionRepository;
import nl.inholland.bank_api.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountService accountService;

    public TransactionService(TransactionRepository transactionRepository, AccountService accountService) {
        this.transactionRepository = transactionRepository;
        this.accountService = accountService;
    }

    public Long postTransaction(TransactionRequestDTO dto) {
        return transactionRepository.save(toTransaction(dto)).getId();
    }

    private Account fetchAccountByIban(String iban) {
        try {
            return accountService.fetchAccountByIban(iban);
        } catch (Exception e) {
            return null;
        }
    }

    private Transaction toTransaction(TransactionRequestDTO dto) {
        Transaction transaction = new Transaction();

        Account sourceAccount = fetchAccountByIban(dto.sourceAccount);
        Account targetAccount = fetchAccountByIban(dto.targetAccount);

        transaction.setSourceAccount(sourceAccount);
        transaction.setTargetAccount(targetAccount);
        transaction.setAmount(dto.amount);
        transaction.setDescription(dto.description);

        if (isTransactionSuccessful(sourceAccount, targetAccount, dto.amount)) {
            transaction.setStatus(Status.SUCCEEDED);
            accountService.updateBalance(sourceAccount, dto.amount, Operation.SUBTRACTION);
            accountService.updateBalance(targetAccount, dto.amount, Operation.ADDITION);
        } else {
            transaction.setStatus(Status.FAILED);
        }

        return transaction;
    }

    private boolean isTransactionSuccessful(Account sourceAccount, Account targetAccount, BigDecimal amount) {
        if (sourceAccount == null || targetAccount == null || sourceAccount == targetAccount) {
            return false;
        }

        // Check absolute limit
        BigDecimal resultingBalance = sourceAccount.getBalance().subtract(amount);
        if (resultingBalance.compareTo(sourceAccount.getAbsoluteLimit()) < 0) {
            return false; // would go below absolute limit
        }

        // Check daily limit
        BigDecimal totalToday = transactionRepository.sumAmountForAccountToday(sourceAccount.getId(), LocalDate.now());
        if (totalToday.add(amount).compareTo(sourceAccount.getDailyLimit()) > 0) {
            return false; // daily limit exceeded
        }

        return true;
    }
}