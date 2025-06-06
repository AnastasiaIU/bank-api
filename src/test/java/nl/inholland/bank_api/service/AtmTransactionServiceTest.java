package nl.inholland.bank_api.service;

import jakarta.persistence.EntityNotFoundException;
import nl.inholland.bank_api.constant.ErrorMessages;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@Import(AtmTransactionService.class)
class AtmTransactionServiceTest {
    @Autowired
    private AtmTransactionService service;

    @MockitoBean
    private AtmTransactionRepository transactionRepository;

    @MockitoBean
    private AccountRepository accountRepository;

    @MockitoBean
    private AtmTransactionMapper mapper;

    private AtmTransactionRequestDTO getValidAtmTransactionRequest() {
        AtmTransactionRequestDTO dto = new AtmTransactionRequestDTO();
        dto.amount = BigDecimal.TEN;
        dto.type = AtmTransactionType.DEPOSIT;
        dto.iban = "NL";
        return dto;
    }

    private Account createAccount(BigDecimal balance) {
        Account account = new Account();
        account.setId(1L);
        account.setIban("NL01INHO0123456789");
        account.setBalance(balance);
        account.setAbsoluteLimit(BigDecimal.ZERO);
        account.setWithdrawLimit(new BigDecimal("500.00"));
        return account;
    }

    private AtmTransaction createTransaction(Account account, AtmTransactionType type, BigDecimal amount) {
        AtmTransaction transaction = new AtmTransaction();
        transaction.setAccount(account);
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setStatus(Status.PENDING);
        return transaction;
    }

    @Test
    void createTransactionSavesAndReturnsDto() {
        AtmTransactionRequestDTO request = getValidAtmTransactionRequest();
        AtmTransaction entity = new AtmTransaction();
        AtmTransaction saved = new AtmTransaction();
        AtmTransactionDTO dto = new AtmTransactionDTO(1L, "NL", 2L, AtmTransactionType.DEPOSIT,
                BigDecimal.TEN, null, "PENDING", null);

        // Mock mapper behaviour and repository save method
        when(mapper.toEntity(any(), any(), any())).thenReturn(entity);
        when(transactionRepository.save(entity)).thenReturn(saved);
        when(mapper.toAtmTransactionDTO(saved)).thenReturn(dto);

        // Assert the returned DTO and verify the save operation
        AtmTransactionDTO result = service.createTransaction(request, new Account(), new User());
        assertEquals(dto, result);
        verify(transactionRepository).save(entity);
    }

    @Test
    void getTransactionReturnsCorrectDto() {
        Account account = createAccount(BigDecimal.ZERO);
        User user = new User();
        user.setId(42L);
        LocalDateTime time = LocalDateTime.now();

        AtmTransaction entity = createTransaction(account, AtmTransactionType.DEPOSIT, BigDecimal.TEN);
        entity.setId(5L);
        entity.setInitiatedBy(user);
        entity.setTimestamp(time);
        entity.setStatus(Status.SUCCEEDED);

        AtmTransactionDTO mockedDto = new AtmTransactionDTO(
                5L,
                "NL01INHO0123456789",
                42L,
                AtmTransactionType.DEPOSIT,
                BigDecimal.TEN,
                time,
                Status.SUCCEEDED.name(),
                null
        );

        // Mock repository and mapper behaviour
        when(transactionRepository.findById(5L)).thenReturn(Optional.of(entity));
        when(mapper.toAtmTransactionDTO(entity)).thenReturn(mockedDto);

        AtmTransactionDTO dto = service.getTransaction(5L);

        // Assert the returned DTO's properties
        assertEquals(5L, dto.id());
        assertEquals("NL01INHO0123456789", dto.iban());
        assertEquals(42L, dto.initiatedBy());
        assertEquals(AtmTransactionType.DEPOSIT, dto.type());
        assertEquals(BigDecimal.TEN, dto.amount());
        assertEquals(time, dto.timestamp());
        assertEquals(Status.SUCCEEDED.name(), dto.status());
        assertNull(dto.failureReason());

        // Verify the repository's findById method was called
        verify(transactionRepository).findById(5L);
    }

    @Test
    void getTransactionThrowsWhenNotFound() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.empty());
        EntityNotFoundException e = assertThrows(EntityNotFoundException.class,
                () -> service.getTransaction(1L));
        assertEquals(ErrorMessages.TRANSACTION_NOT_FOUND, e.getMessage());
    }

    @Test
    void processTransactionSucceedsForDeposit() {
        Account account = createAccount(new BigDecimal("100.00"));
        AtmTransaction transaction = createTransaction(account, AtmTransactionType.DEPOSIT, new BigDecimal("50.00"));

        // Mock repository behaviour
        when(transactionRepository.findByStatus(Status.PENDING)).thenReturn(List.of(transaction));
        when(transactionRepository.save(any(AtmTransaction.class))).thenReturn(transaction);

        service.processTransaction(transaction);

        // Verify the account balance and transaction status
        assertEquals(new BigDecimal("150.00"), account.getBalance());
        assertEquals(Status.SUCCEEDED, transaction.getStatus());
        assertNull(transaction.getFailureReason());
        verify(accountRepository).save(account);
        verify(transactionRepository).save(transaction);
    }

    @Test
    void processTransactionSucceedsForWithdrawal() {
        Account account = createAccount(new BigDecimal("200.00"));
        AtmTransaction transaction = createTransaction(account, AtmTransactionType.WITHDRAW, new BigDecimal("100.00"));

        // Mock repository behaviour
        when(transactionRepository.findByStatus(Status.PENDING)).thenReturn(List.of(transaction));
        when(transactionRepository.sumTodayWithdrawalsByAccount(anyLong(), any(LocalDate.class))).thenReturn(new BigDecimal("100.00"));
        when(transactionRepository.save(any(AtmTransaction.class))).thenReturn(transaction);

        service.processTransaction(transaction);

        // Verify the account balance and transaction status
        assertEquals(new BigDecimal("100.00"), account.getBalance());
        assertEquals(Status.SUCCEEDED, transaction.getStatus());
        assertNull(transaction.getFailureReason());
        verify(accountRepository).save(account);
        verify(transactionRepository).save(transaction);
    }

    @Test
    void processTransactionFailsWhenInsufficientBalance() {
        Account account = createAccount(new BigDecimal("20.00"));
        AtmTransaction transaction = createTransaction(account, AtmTransactionType.WITHDRAW, new BigDecimal("50.00"));

        // Mock repository behaviour
        when(transactionRepository.findByStatus(Status.PENDING)).thenReturn(List.of(transaction));
        when(transactionRepository.save(any(AtmTransaction.class))).thenReturn(transaction);

        service.processTransaction(transaction);

        // Verify the account balance and transaction status
        assertEquals(new BigDecimal("20.00"), account.getBalance());
        assertEquals(Status.FAILED, transaction.getStatus());
        assertEquals(ErrorMessages.INSUFFICIENT_BALANCE, transaction.getFailureReason());
        verify(accountRepository, never()).save(any());
        verify(transactionRepository).save(transaction);
    }

    @Test
    void processTransactionFailsWhenDailyLimitExceeded() {
        Account account = createAccount(new BigDecimal("1000.00"));
        account.setAbsoluteLimit(new BigDecimal("-1000.00"));
        AtmTransaction transaction = createTransaction(account, AtmTransactionType.WITHDRAW, new BigDecimal("200.00"));

        // Mock repository behaviour
        when(transactionRepository.findByStatus(Status.PENDING)).thenReturn(List.of(transaction));
        when(transactionRepository.sumTodayWithdrawalsByAccount(anyLong(), any(LocalDate.class))).thenReturn(new BigDecimal("400.00"));
        when(transactionRepository.save(any(AtmTransaction.class))).thenReturn(transaction);

        service.processTransaction(transaction);

        // Verify the account balance and transaction status
        assertEquals(new BigDecimal("1000.00"), account.getBalance());
        assertEquals(Status.FAILED, transaction.getStatus());
        assertEquals(ErrorMessages.DAILY_WITHDRAWAL_LIMIT_EXCEEDED, transaction.getFailureReason());
        verify(accountRepository, never()).save(any());
        verify(transactionRepository).save(transaction);
    }
}