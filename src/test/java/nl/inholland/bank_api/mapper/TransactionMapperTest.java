package nl.inholland.bank_api.mapper;

import nl.inholland.bank_api.model.dto.CombinedTransactionDTO;
import nl.inholland.bank_api.model.dto.TransactionRequestDTO;
import nl.inholland.bank_api.model.entities.Account;
import nl.inholland.bank_api.model.entities.Transaction;
import nl.inholland.bank_api.model.entities.User;
import nl.inholland.bank_api.model.enums.Status;
import nl.inholland.bank_api.model.enums.*;
import nl.inholland.bank_api.repository.TransactionRepository;
import nl.inholland.bank_api.service.AccountService;
import nl.inholland.bank_api.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class TransactionMapperTest {

    private AccountService accountService = mock(AccountService.class);
    private UserService userService = mock(UserService.class);
    private TransactionRepository transactionRepository = mock(TransactionRepository.class);
    private TransactionMapper mapper = new TransactionMapper(accountService, userService, transactionRepository);

    private TransactionRequestDTO getValidRequest() {
        TransactionRequestDTO dto = new TransactionRequestDTO();
        dto.setSourceAccount("NL01INHO0000000001");
        dto.setTargetAccount("NL01INHO0000000002");
        dto.setAmount(BigDecimal.valueOf(100));
        dto.setDescription("Test payment");
        dto.setInitiatedBy(1L);
        return dto;
    }

    @Test
    void toCombinedDTO_MapsFieldsCorrectly() {
        Transaction t = new Transaction();
        Account source = new Account(); source.setIban("SRC");
        Account target = new Account(); target.setIban("TGT");

        t.setId(1L);
        t.setSourceAccount(source);
        t.setTargetAccount(target);
        t.setAmount(new BigDecimal("50.00"));
        t.setDescription("Transfer");
        t.setTimestamp(LocalDateTime.now());
        t.setStatus(Status.SUCCEEDED);

        CombinedTransactionDTO dto = mapper.toCombinedDTO(t);

        assertEquals(t.getId(), dto.id);
        assertEquals("TRANSFER", dto.type);
        assertEquals("SRC", dto.sourceIban);
        assertEquals("TGT", dto.targetIban);
        assertEquals(t.getAmount(), dto.amount);
        assertEquals("Transfer", dto.description);
        assertEquals(t.getTimestamp(), dto.timestamp);
        assertEquals(Status.SUCCEEDED, dto.status);
    }

    @Test
    void toEntity_SetsStatusFailed_WhenInsufficientFunds() {
        TransactionRequestDTO dto = getValidRequest();

        Account source = new Account();
        source.setIban(dto.getSourceAccount());
        source.setBalance(BigDecimal.ZERO);
        source.setAbsoluteLimit(BigDecimal.ZERO);

        Account target = new Account();
        target.setIban(dto.getTargetAccount());

        User user = new User(); user.setId(dto.getInitiatedBy());

        when(accountService.fetchAccountByIban(dto.getSourceAccount())).thenReturn(source);
        when(accountService.fetchAccountByIban(dto.getTargetAccount())).thenReturn(target);
        when(userService.getUserById(dto.getInitiatedBy())).thenReturn(user);

        Transaction transaction = mapper.toEntity(dto);

        assertEquals(Status.FAILED, transaction.getStatus());
        verify(accountService, never()).updateBalance(any(), any(), any());
    }

    @Test
    void toEntity_ShouldMapAndSucceedTransaction_WhenLimitsAreValid() {
        // Arrange
        TransactionRequestDTO dto = new TransactionRequestDTO();
        dto.setSourceAccount("NL01BANK0001");
        dto.setTargetAccount("NL01BANK0002");
        dto.setAmount(new BigDecimal("100.00"));
        dto.setDescription("Rent");
        dto.setInitiatedBy(1L);

        User sender = User.builder()
                .id(dto.getInitiatedBy())
                .firstName("John")
                .lastName("Doe")
                .email("123@mail.com")
                .password("123")
                .bsn("123456789")
                .phoneNumber("+1234567890")
                .isApproved(UserAccountStatus.APPROVED)
                .role(UserRole.CUSTOMER)
                .build();

        User receiver = User.builder()
                .firstName("Bea")
                .lastName("Summers")
                .email("1234@mail.com")
                .password("1234")
                .bsn("123456783")
                .phoneNumber("+1234567830")
                .isApproved(UserAccountStatus.APPROVED)
                .role(UserRole.CUSTOMER)
                .build();

        Account source = Account.builder()
                .id(1L)
                .iban(dto.getSourceAccount())
                .user(sender)
                .balance(new BigDecimal("1000.00"))
                .absoluteLimit(new BigDecimal("-200.00"))
                .dailyLimit(new BigDecimal("500.00"))
                .status(AccountStatus.ACTIVE)
                .type(AccountType.CHECKING)
                .build();

        Account target = Account.builder()
                .id(2L)
                .iban(dto.getTargetAccount())
                .user(receiver)
                .balance(new BigDecimal("500.00"))
                .absoluteLimit(new BigDecimal("5000.00"))
                .dailyLimit(new BigDecimal("300.00"))
                .status(AccountStatus.ACTIVE)
                .type(AccountType.CHECKING)
                .build();

        when(accountService.fetchAccountByIban(dto.getSourceAccount())).thenReturn(source);
        when(accountService.fetchAccountByIban(dto.getTargetAccount())).thenReturn(target);
        when(userService.getUserById(dto.getInitiatedBy())).thenReturn(sender);
        when(transactionRepository.sumAmountForAccountToday(1L, LocalDate.now()))
                .thenReturn(new BigDecimal("100.00"));

        // Act
        Transaction result = mapper.toEntity(dto);

        // Assert
        assertEquals(dto.getSourceAccount(), result.getSourceAccount().getIban());
        assertEquals(dto.getTargetAccount(), result.getTargetAccount().getIban());
        assertEquals(dto.getInitiatedBy(), result.getInitiatedBy().getId());
        assertEquals(dto.getAmount(), result.getAmount());
        assertEquals(dto.getDescription(), result.getDescription());
        assertEquals(Status.SUCCEEDED, result.getStatus());
        verify(accountService).updateBalance(source, dto.getAmount(), Operation.SUBTRACTION);
        verify(accountService).updateBalance(target, dto.getAmount(), Operation.ADDITION);
    }

    @Test
    void toEntity_ShouldFailTransaction_WhenAbsoluteLimitExceeded() {
        // Arrange
        TransactionRequestDTO dto = new TransactionRequestDTO();
        dto.setSourceAccount("NL01BANK0001");
        dto.setTargetAccount("NL01BANK0002");
        dto.setAmount(new BigDecimal("1200.00"));
        dto.setInitiatedBy(1L);

        User sender = User.builder()
                .id(dto.getInitiatedBy())
                .firstName("John")
                .lastName("Doe")
                .email("123@mail.com")
                .password("123")
                .bsn("123456789")
                .phoneNumber("+1234567890")
                .isApproved(UserAccountStatus.APPROVED)
                .role(UserRole.CUSTOMER)
                .build();

        User receiver = User.builder()
                .firstName("Bea")
                .lastName("Summers")
                .email("1234@mail.com")
                .password("1234")
                .bsn("123456783")
                .phoneNumber("+1234567830")
                .isApproved(UserAccountStatus.APPROVED)
                .role(UserRole.CUSTOMER)
                .build();

        Account source = Account.builder()
                .id(1L)
                .iban(dto.getSourceAccount())
                .user(sender)
                .balance(new BigDecimal("1000.00"))
                .absoluteLimit(new BigDecimal("-200.00"))
                .dailyLimit(new BigDecimal("500.00"))
                .status(AccountStatus.ACTIVE)
                .type(AccountType.CHECKING)
                .build();

        Account target = Account.builder()
                .id(2L)
                .iban(dto.getTargetAccount())
                .user(receiver)
                .balance(new BigDecimal("500.00"))
                .absoluteLimit(new BigDecimal("5000.00"))
                .dailyLimit(new BigDecimal("300.00"))
                .status(AccountStatus.ACTIVE)
                .type(AccountType.CHECKING)
                .build();

        when(accountService.fetchAccountByIban(dto.getSourceAccount())).thenReturn(source);
        when(accountService.fetchAccountByIban(dto.getTargetAccount())).thenReturn(target);
        when(userService.getUserById(dto.getInitiatedBy())).thenReturn(sender);
        when(transactionRepository.sumAmountForAccountToday(1L, LocalDate.now())).thenReturn(BigDecimal.ZERO);

        // Act
        Transaction result = mapper.toEntity(dto);

        // Assert
        assertEquals(Status.FAILED, result.getStatus());
        verify(accountService, never()).updateBalance(any(), any(), any());
    }

    @Test
    void toEntity_ShouldFailTransaction_WhenDailyLimitExceeded() {
        // Arrange
        TransactionRequestDTO dto = new TransactionRequestDTO();
        dto.setSourceAccount("NL01BANK0001");
        dto.setTargetAccount("NL01BANK0002");
        dto.setAmount(new BigDecimal("400.00"));
        dto.setInitiatedBy(1L);

        User sender = User.builder()
                .id(dto.getInitiatedBy())
                .firstName("John")
                .lastName("Doe")
                .email("123@mail.com")
                .password("123")
                .bsn("123456789")
                .phoneNumber("+1234567890")
                .isApproved(UserAccountStatus.APPROVED)
                .role(UserRole.CUSTOMER)
                .build();

        User receiver = User.builder()
                .firstName("Bea")
                .lastName("Summers")
                .email("1234@mail.com")
                .password("1234")
                .bsn("123456783")
                .phoneNumber("+1234567830")
                .isApproved(UserAccountStatus.APPROVED)
                .role(UserRole.CUSTOMER)
                .build();

        Account source = Account.builder()
                .id(1L)
                .iban(dto.getSourceAccount())
                .user(sender)
                .balance(new BigDecimal("1000.00"))
                .absoluteLimit(new BigDecimal("-200.00"))
                .dailyLimit(new BigDecimal("500.00"))
                .status(AccountStatus.ACTIVE)
                .type(AccountType.CHECKING)
                .build();

        Account target = Account.builder()
                .id(2L)
                .iban(dto.getTargetAccount())
                .user(receiver)
                .balance(new BigDecimal("500.00"))
                .absoluteLimit(new BigDecimal("5000.00"))
                .dailyLimit(new BigDecimal("300.00"))
                .status(AccountStatus.ACTIVE)
                .type(AccountType.CHECKING)
                .build();

        when(accountService.fetchAccountByIban(dto.getSourceAccount())).thenReturn(source);
        when(accountService.fetchAccountByIban(dto.getTargetAccount())).thenReturn(target);
        when(userService.getUserById(dto.getInitiatedBy())).thenReturn(sender);
        when(transactionRepository.sumAmountForAccountToday(1L, LocalDate.now()))
                .thenReturn(new BigDecimal("200.00"));

        // Act
        Transaction result = mapper.toEntity(dto);

        // Assert
        assertEquals(Status.FAILED, result.getStatus());
        verify(accountService, never()).updateBalance(any(), any(), any());
    }
}