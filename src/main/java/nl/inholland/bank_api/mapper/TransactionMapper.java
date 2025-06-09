package nl.inholland.bank_api.mapper;

import nl.inholland.bank_api.model.dto.CombinedTransactionDTO;
import nl.inholland.bank_api.model.dto.TransactionRequestDTO;
import nl.inholland.bank_api.model.dto.TransactionResponseDTO;
import nl.inholland.bank_api.model.entities.Account;
import nl.inholland.bank_api.model.entities.Transaction;
import nl.inholland.bank_api.model.entities.User;
import nl.inholland.bank_api.model.enums.Operation;
import nl.inholland.bank_api.model.enums.Status;
import nl.inholland.bank_api.repository.TransactionRepository;
import nl.inholland.bank_api.service.AccountService;
import nl.inholland.bank_api.service.UserService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class TransactionMapper {

    AccountService accountService;
    UserService userService;
    TransactionRepository transactionRepository;

    public TransactionMapper(AccountService accountService, UserService userService, TransactionRepository transactionRepository) {
        this.accountService = accountService;
        this.userService = userService;
        this.transactionRepository = transactionRepository;
    }

    public CombinedTransactionDTO toCombinedDTO(Transaction t) {
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
    }

    public Transaction toEntity(TransactionRequestDTO dto) {
        Transaction transaction = new Transaction();

        Account sourceAccount = accountService.fetchAccountByIban(dto.sourceAccount);
        Account targetAccount = accountService.fetchAccountByIban(dto.targetAccount);
        User initiatedBy = userService.getUserById(dto.initiatedBy);

        transaction.setSourceAccount(sourceAccount);
        transaction.setTargetAccount(targetAccount);
        transaction.setInitiatedBy(initiatedBy);
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