package nl.inholland.bank_api.service;

import nl.inholland.bank_api.mapper.AtmTransactionMapper;
import nl.inholland.bank_api.model.dto.AtmTransactionDTO;
import nl.inholland.bank_api.model.dto.AtmTransactionRequestDTO;
import nl.inholland.bank_api.model.entities.Account;
import nl.inholland.bank_api.model.entities.AtmTransaction;
import nl.inholland.bank_api.model.entities.User;
import nl.inholland.bank_api.model.enums.AtmTransactionType;
import nl.inholland.bank_api.model.enums.Status;
import nl.inholland.bank_api.repository.AccountRepository;
import nl.inholland.bank_api.repository.AtmTransactionRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class AtmTransactionService {
    private final AtmTransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final AtmTransactionMapper transactionMapper;

    public AtmTransactionService(
            AtmTransactionRepository atmTransactionRepository,
            AccountRepository accountRepository,
            AtmTransactionMapper atmTransactionMapper
    ) {
        this.transactionRepository = atmTransactionRepository;
        this.accountRepository = accountRepository;
        this.transactionMapper = atmTransactionMapper;
    }

    public AtmTransactionDTO createTransaction(AtmTransactionRequestDTO dto, Account account, User initiatedBy) {
        AtmTransaction transaction = transactionMapper.toEntity(dto, account, initiatedBy);
        AtmTransaction saved = transactionRepository.save(transaction);

        return transactionMapper.toAtmTransactionDTO(saved);
    }

    @Scheduled(fixedRate = 10000) // Every 10 seconds
    public void processPendingTransactions() {
        List<AtmTransaction> pendingTransactions = transactionRepository.findByStatus(Status.PENDING);

        for (AtmTransaction transaction : pendingTransactions) {
            processTransaction(transaction);
        }
    }

    private void processTransaction(AtmTransaction transaction) {
        Account account = transaction.getAccount();
        BigDecimal amount = transaction.getAmount();

        if (transaction.getType() == AtmTransactionType.DEPOSIT) {
            BigDecimal newBalance = account.getBalance().add(amount);
            updateStatus(transaction, Status.SUCCEEDED, null);
            updateBalance(transaction, newBalance);
        } else {

            // Check against absolute limit
            BigDecimal projectedBalance = account.getBalance().subtract(amount);
            if (projectedBalance.compareTo(account.getAbsoluteLimit()) < 0) {
                updateStatus(transaction, Status.FAILED, "Insufficient balance");
            } else {

                // Check the daily withdrawal limit
                BigDecimal todayTotal = getTodayTotal(account).add(amount);
                boolean isDailyLimitExceeded = todayTotal.compareTo(account.getWithdrawLimit()) > 0;

                if (isDailyLimitExceeded) {
                    updateStatus(transaction, Status.FAILED, "Daily withdrawal limit exceeded");
                } else {
                    updateStatus(transaction, Status.SUCCEEDED, null);
                    updateBalance(transaction, projectedBalance);
                }
            }
        }

        transactionRepository.save(transaction);
    }

    private void updateStatus(AtmTransaction transaction, Status status, String failureReason) {
        transaction.setStatus(status);
        transaction.setFailureReason(failureReason);
    }

    private void updateBalance(AtmTransaction transaction, BigDecimal balance) {
        Account account = transaction.getAccount();
        account.setBalance(balance);
        accountRepository.save(account);
    }

    private BigDecimal getTodayTotal(Account account) {
        BigDecimal todayTotal = transactionRepository.sumTodayWithdrawalsByAccount(account.getId(), LocalDate.now());
        return todayTotal != null ? todayTotal : BigDecimal.ZERO;
    }
}
