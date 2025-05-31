package nl.inholland.bank_api.service;

import jakarta.persistence.EntityNotFoundException;
import nl.inholland.bank_api.mapper.AtmTransactionMapper;
import nl.inholland.bank_api.mapper.TransactionMapper;
import nl.inholland.bank_api.model.dto.*;
import nl.inholland.bank_api.model.entities.Account;
import nl.inholland.bank_api.model.entities.Transaction;
import nl.inholland.bank_api.model.enums.Operation;
import nl.inholland.bank_api.model.enums.Status;
import nl.inholland.bank_api.repository.AccountRepository;
import nl.inholland.bank_api.repository.AtmTransactionRepository;
import nl.inholland.bank_api.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AtmTransactionRepository atmTransactionRepository;
    private final AtmTransactionMapper atmTransactionMapper;
    private final TransactionMapper transactionMapper;
    private final AccountRepository accountRepository;

    public TransactionService(TransactionRepository transactionRepository, AtmTransactionRepository atmTransactionRepository, AtmTransactionMapper atmTransactionMapper, TransactionMapper transactionMapper, AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.atmTransactionRepository = atmTransactionRepository;
        this.atmTransactionMapper = atmTransactionMapper;
        this.transactionMapper = transactionMapper;
        this.accountRepository = accountRepository;
    }

    public TransactionResponseDTO createTransaction(TransactionRequestDTO dto, Account sourceAccount, Account targetAccount) {
        Transaction transaction = transactionMapper.toTransactionEntity(dto,sourceAccount, targetAccount);
        Transaction saved = transactionRepository.save(transaction);
        return transactionMapper.toTransactionDTO(saved);
    }

    public void processPendingTransactions() {
        List<Transaction> pendingTransactions = transactionRepository.findByStatus(Status.PENDING);

        for (Transaction transaction : pendingTransactions) {
            processTransaction(transaction);
        }
    }

    private void processTransaction(Transaction transaction) {
        Account source = transaction.getSourceAccount();
        Account target = transaction.getTargetAccount();
        BigDecimal amount = transaction.getAmount();

        if (source == null || target == null || source.equals(target)) {
            updateStatus(transaction, Status.FAILED, "Invalid source or target account");
        } else {
            BigDecimal projectedBalance = source.getBalance().subtract(amount);
            if (projectedBalance.compareTo(source.getAbsoluteLimit()) < 0) {
                updateStatus(transaction, Status.FAILED, "Absolute limit exceeded");
            } else {
                BigDecimal todayTotal = transactionRepository.sumAmountForAccountToday(source.getId(), LocalDate.now());
                boolean isDailyLimitExceeded = todayTotal.add(amount).compareTo(source.getDailyLimit()) > 0;

                if (isDailyLimitExceeded) {
                    updateStatus(transaction, Status.FAILED, "Daily limit exceeded");
                } else {
                    updateStatus(transaction, Status.SUCCEEDED, null);

                    updateBalance(source, amount, Operation.SUBTRACTION);
                    updateBalance(target, amount, Operation.ADDITION);

                    accountRepository.save(source);
                    accountRepository.save(target);
                }
            }
        }

        transactionRepository.save(transaction);
    }

    private void updateStatus(Transaction transaction, Status status, String failureReason) {
        transaction.setStatus(status);
        transaction.setFailureReason(failureReason);
    }

    private void updateBalance(Account account, BigDecimal amount, Operation operation) {
        if (operation == Operation.ADDITION) {
            account.setBalance(account.getBalance().add(amount));
        } else if (operation == Operation.SUBTRACTION) {
            account.setBalance(account.getBalance().subtract(amount));
        }
    }

    public TransactionResponseDTO getTransaction(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found with id: " + id));

        return transactionMapper.toTransactionDTO(transaction);
    }

    public List<CombinedTransactionDTO> getFilteredTransactions(Long accountId, TransactionFilterDTO filterDTO) {
        Stream<CombinedTransactionDTO> combinedStream = getCombinedTransactionStream(accountId);
        return applyFilters(combinedStream, filterDTO).collect(Collectors.toList());
    }

    private Stream<CombinedTransactionDTO> getCombinedTransactionStream(Long accountId) {
        Stream<CombinedTransactionDTO> transferStream = transactionRepository
                .findBySourceAccount_IdOrTargetAccount_Id(accountId, accountId)
                .stream()
                .map(transactionMapper::toCombinedDTO);

        Stream<CombinedTransactionDTO> atmStream = atmTransactionRepository
                .findByAccountId(accountId)
                .stream()
                .map(atmTransactionMapper::toCombinedDTO);
        return Stream.concat(transferStream, atmStream);
    }

    private Stream<CombinedTransactionDTO> applyFilters(Stream<CombinedTransactionDTO> stream, TransactionFilterDTO filterDTO) {
        return stream.filter(dto -> matchesFilters(dto, filterDTO));
    }

    private boolean matchesFilters(CombinedTransactionDTO dto, TransactionFilterDTO filterDTO) {
        return matchesDate(dto.timestamp, filterDTO) &&
                matchesAmount(dto.amount, filterDTO) &&
                matchesIbans(dto.sourceIban, dto.targetIban, filterDTO);
    }

    private boolean matchesDate(LocalDateTime timestamp, TransactionFilterDTO filter) {
        if (filter.getStartDate() != null && !filter.getStartDate().isBlank()) {
            LocalDate start = LocalDate.parse(filter.getStartDate());
            if (timestamp.toLocalDate().isBefore(start)) return false;
        }
        if (filter.getEndDate() != null && !filter.getEndDate().isBlank()) {
            LocalDate end = LocalDate.parse(filter.getEndDate());
            if (timestamp.toLocalDate().isAfter(end)) return false;
        }
        return true;
    }

    private boolean matchesAmount(BigDecimal amount, TransactionFilterDTO filter) {
        System.out.println("Filtering amount: dto=" + amount + ", filterAmount=" + filter.getAmount() + ", comparison=" + filter.getComparison());
        if (filter.getAmount() != null && filter.getComparison() != null) {
            int cmp = amount.compareTo(filter.getAmount());
            boolean result = switch (filter.getComparison()) {
                case "lt" -> cmp < 0;
                case "gt" -> cmp > 0;
                case "eq" -> cmp == 0;
                default -> true;
            };
            System.out.println("Amount filter result: " + result);
            return result;
        }
        return true;
    }

    private boolean matchesIbans(String sourceIban, String targetIban, TransactionFilterDTO filter) {
        if (filter.getSourceIban() != null && !filter.getSourceIban().isBlank() && !filter.getSourceIban().equals(sourceIban))
            return false;
        if (filter.getTargetIban() != null && !filter.getTargetIban().isBlank() && !filter.getTargetIban().equals(targetIban))
            return false;
        return true;
    }
}