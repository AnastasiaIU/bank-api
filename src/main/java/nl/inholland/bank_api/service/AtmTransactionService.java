package nl.inholland.bank_api.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import nl.inholland.bank_api.constant.ErrorMessages;
import nl.inholland.bank_api.mapper.AtmTransactionMapper;
import nl.inholland.bank_api.model.dto.AtmHistoryTransactionDTO;
import nl.inholland.bank_api.model.dto.AtmTransactionDTO;
import nl.inholland.bank_api.model.dto.AtmTransactionRequestDTO;
import nl.inholland.bank_api.model.entities.Account;
import nl.inholland.bank_api.model.entities.AtmTransaction;
import nl.inholland.bank_api.model.entities.User;
import nl.inholland.bank_api.model.enums.AtmTransactionType;
import nl.inholland.bank_api.model.enums.Status;
import nl.inholland.bank_api.repository.AccountRepository;
import nl.inholland.bank_api.repository.AtmTransactionRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class AtmTransactionService {
    private final AtmTransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final AtmTransactionMapper transactionMapper;
    private final SecurityService securityService;

    public AtmTransactionService(
            AtmTransactionRepository atmTransactionRepository,
            AccountRepository accountRepository,
            AtmTransactionMapper atmTransactionMapper,
            SecurityService securityService) {
        this.transactionRepository = atmTransactionRepository;
        this.accountRepository = accountRepository;
        this.transactionMapper = atmTransactionMapper;
        this.securityService = securityService;
    }

    public List<AtmHistoryTransactionDTO> getAllTransactions() {
        return transactionRepository.findAll().stream().map(transactionMapper::toAtmHistoryTransactionDTO).toList();
    }

    @PreAuthorize("@securityService.isOwnerOfAccount(#dto.iban)")
    public AtmTransactionDTO createTransaction(AtmTransactionRequestDTO dto, Account account, User initiatedBy) {
        AtmTransaction transaction = transactionMapper.toEntity(dto, account, initiatedBy);
        AtmTransaction saved = transactionRepository.save(transaction);

        return transactionMapper.toAtmTransactionDTO(saved);
    }

    @PreAuthorize("@securityService.isOwnerOfTransactionAccount(#id)")
    public AtmTransactionDTO getTransaction(Long id) {
        AtmTransaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessages.TRANSACTION_NOT_FOUND));

        return transactionMapper.toAtmTransactionDTO(transaction);
    }

    @Transactional
    public void processTransaction(AtmTransaction transaction) {
        Account account = transaction.getAccount();
        BigDecimal amount = transaction.getAmount();

        if (transaction.getType() == AtmTransactionType.DEPOSIT) {
            BigDecimal newBalance = account.getBalance().add(amount);
            setTransactionStatus(transaction, Status.SUCCEEDED, null);
            updateBalance(transaction, newBalance);
        } else {

            // Check against absolute limit
            BigDecimal projectedBalance = account.getBalance().subtract(amount);
            if (projectedBalance.compareTo(account.getAbsoluteLimit()) < 0) {
                setTransactionStatus(transaction, Status.FAILED, ErrorMessages.INSUFFICIENT_BALANCE);
            } else {

                // Check the daily withdrawal limit
                BigDecimal todayTotal = getTodayTotal(account).add(amount);
                boolean isDailyLimitExceeded = todayTotal.compareTo(account.getWithdrawLimit()) > 0;

                if (isDailyLimitExceeded) {
                    setTransactionStatus(transaction, Status.FAILED, ErrorMessages.DAILY_WITHDRAWAL_LIMIT_EXCEEDED);
                } else {
                    setTransactionStatus(transaction, Status.SUCCEEDED, null);
                    updateBalance(transaction, projectedBalance);
                }
            }
        }

        transactionRepository.save(transaction);
    }

    public List<AtmTransaction> getPendingTransactions() {
        return transactionRepository.findByStatus(Status.PENDING);
    }

    private void setTransactionStatus(AtmTransaction transaction, Status status, String failureReason) {
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
