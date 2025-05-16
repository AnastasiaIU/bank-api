package nl.inholland.bank_api.service;

import nl.inholland.bank_api.mapper.AtmTransactionMapper;
import nl.inholland.bank_api.mapper.TransactionMapper;
import nl.inholland.bank_api.model.dto.CombinedTransactionDTO;
import nl.inholland.bank_api.model.dto.TransactionFilterDTO;
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
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final AtmTransactionRepository atmTransactionRepository;

    public TransactionService(TransactionRepository transactionRepository, AccountService accountService, AtmTransactionRepository atmTransactionRepository, AtmTransactionMapper atmTransactionMapper, TransactionMapper transactionMapper) {
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

    private boolean isTransactionSuccessful(Account sourceAccount, Account targetAccount, BigDecimal amount) {
        return sourceAccount != null &&
                targetAccount != null &&
                sourceAccount != targetAccount &&
                sourceAccount.getBalance().compareTo(amount) > 0;
    }
}