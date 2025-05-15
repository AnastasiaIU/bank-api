package nl.inholland.bank_api.service;

import nl.inholland.bank_api.model.dto.CombinedTransactionDTO;
import nl.inholland.bank_api.model.dto.TransactionRequestDTO;
import nl.inholland.bank_api.model.dto.TransactionResponseDTO;
import nl.inholland.bank_api.model.entities.Account;
import nl.inholland.bank_api.model.entities.AtmTransaction;
import nl.inholland.bank_api.model.entities.Transaction;
import nl.inholland.bank_api.model.enums.AtmTransactionType;
import nl.inholland.bank_api.model.enums.Operation;
import nl.inholland.bank_api.model.enums.Status;
import nl.inholland.bank_api.repository.AtmTransactionRepository;
import nl.inholland.bank_api.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.stream;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final AtmTransactionRepository atmTransactionRepository;

    public TransactionService(TransactionRepository transactionRepository, AccountService accountService, AtmTransactionRepository atmTransactionRepository) {
        this.transactionRepository = transactionRepository;
        this.accountService = accountService;
        this.atmTransactionRepository = atmTransactionRepository;
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

    public List<CombinedTransactionDTO> getFilteredTransactions(Long accountId, String onDate, String before, String after, BigDecimal amount, String comparison, String sourceIban, String targetIban) {
        List<CombinedTransactionDTO> result = new ArrayList<>();

        transactionRepository.findBySourceAccount_IdOrTargetAccount_Id(accountId, accountId)
                .stream()
                .map(t -> {
                    CombinedTransactionDTO dto = new CombinedTransactionDTO();
                    dto.id = t.getId();
                    dto.type = "TRANSFER";
                    dto.sourceIban = t.getSourceAccount().getIban();
                    dto.targetIban = t.getTargetAccount().getIban();
                    dto.amount = t.getAmount();
                    dto.description = t.getDescription();
                    dto.timestamp = t.getTimestamp();
                    dto.status = t.getStatus();
                    return dto;
                })
                .filter(dto -> matchesFilters(dto, onDate, before, after, amount, comparison, sourceIban, targetIban))
                .forEach(result::add);

        atmTransactionRepository.findByAccountId(accountId)
                .stream()
                .map(atm -> {
                    CombinedTransactionDTO dto = new CombinedTransactionDTO();
                    dto.id = atm.getId();
                    dto.type = "ATM";
                    dto.sourceIban = atm.getType() == AtmTransactionType.WITHDRAW ? atm.getAccount().getIban() : null;
                    dto.targetIban = atm.getType() == AtmTransactionType.DEPOSIT ? atm.getAccount().getIban() : null;
                    dto.amount = atm.getAmount();
                    dto.timestamp = atm.getTimestamp();
                    dto.status = atm.getStatus();
                    dto.failureReason = atm.getFailureReason();
                    dto.description = atm.getType().name();
                    return dto;
                })
                .filter(dto -> matchesFilters(dto, onDate, before, after, amount, comparison, sourceIban, targetIban))
                .forEach(result::add);

        return result;
    }

    private boolean matchesFilters(CombinedTransactionDTO dto, String onDate, String before, String after,
                                   BigDecimal amount, String comparison,
                                   String sourceIban, String targetIban) {
        LocalDateTime ts = dto.timestamp;

        if (onDate != null && !ts.toLocalDate().isEqual(LocalDate.parse(onDate)))
            return false;

        if (before != null && ts.isAfter(LocalDateTime.parse(before)))
            return false;

        if (after != null && ts.isBefore(LocalDateTime.parse(after)))
            return false;

        if (amount != null && comparison != null) {
            int cmp = dto.amount.compareTo(amount);
            return switch (comparison) {
                case "lt" -> cmp < 0;
                case "gt" -> cmp > 0;
                case "eq" -> cmp == 0;
                default -> true;
            };
        }

        if (sourceIban != null && (dto.sourceIban == null || !dto.sourceIban.equals(sourceIban)))
            return false;

        if (targetIban != null && (dto.targetIban == null || !dto.targetIban.equals(targetIban)))
            return false;

        return true;
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