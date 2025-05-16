package nl.inholland.bank_api.service;

import nl.inholland.bank_api.model.dto.TransactionRequestDTO;
import nl.inholland.bank_api.model.dto.TransactionResponseDTO;
import nl.inholland.bank_api.model.entities.Account;
import nl.inholland.bank_api.model.entities.Transaction;
import nl.inholland.bank_api.model.enums.Operation;
import nl.inholland.bank_api.model.enums.Status;
import nl.inholland.bank_api.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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

    private Transaction toTransaction(TransactionRequestDTO dto) {
        Transaction transaction = new Transaction();

        Account sourceAccount = accountService.fetchAccountByIban(dto.sourceAccount);
        Account targetAccount = accountService.fetchAccountByIban(dto.targetAccount);

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

    public List<TransactionResponseDTO> getTransactionsForAccount(Long accountId) {
        List<Transaction> transactions = transactionRepository.findBySourceAccount_IdOrTargetAccount_Id(accountId, accountId);
        return transactions.stream()
                .map(this::toResponseDTO)
                .toList();
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

    private TransactionResponseDTO toResponseDTO(Transaction transaction) {
        TransactionResponseDTO dto = new TransactionResponseDTO();
        dto.sourceAccount = transaction.getSourceAccount().getId();
        dto.targetAccount = transaction.getTargetAccount().getId();
        dto.amount = transaction.getAmount();
        dto.description = transaction.getDescription();
        dto.status = transaction.getStatus();
        dto.sourceIban = transaction.getSourceAccount().getIban();
        dto.targetIban = transaction.getTargetAccount().getIban();
        dto.timestamp = transaction.getTimestamp();
        return dto;
    }
}