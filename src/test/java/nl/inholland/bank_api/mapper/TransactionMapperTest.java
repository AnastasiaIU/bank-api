package nl.inholland.bank_api.mapper;

import nl.inholland.bank_api.model.dto.CombinedTransactionDTO;
import nl.inholland.bank_api.model.dto.TransactionRequestDTO;
import nl.inholland.bank_api.model.entities.Account;
import nl.inholland.bank_api.model.entities.Transaction;
import nl.inholland.bank_api.model.entities.User;
import nl.inholland.bank_api.model.enums.Status;
import nl.inholland.bank_api.repository.TransactionRepository;
import nl.inholland.bank_api.service.AccountService;
import nl.inholland.bank_api.service.UserService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class TransactionMapperTest {

    private AccountService accountService = mock(AccountService.class);
    private UserService userService = mock(UserService.class);
    private TransactionRepository transactionRepository = mock(TransactionRepository.class);
    private TransactionMapper mapper = new TransactionMapper(accountService, userService, transactionRepository);

    private TransactionRequestDTO getValidRequest() {
        TransactionRequestDTO dto = new TransactionRequestDTO();
        dto.sourceAccount = "NL01INHO0000000001";
        dto.targetAccount = "NL01INHO0000000002";
        dto.amount = BigDecimal.valueOf(100);
        dto.description = "Test payment";
        dto.initiatedBy = 1L;
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
        source.setIban(dto.sourceAccount);
        source.setBalance(BigDecimal.ZERO);
        source.setAbsoluteLimit(BigDecimal.ZERO);

        Account target = new Account();
        target.setIban(dto.targetAccount);

        User user = new User(); user.setId(dto.initiatedBy);

        when(accountService.fetchAccountByIban(dto.sourceAccount)).thenReturn(source);
        when(accountService.fetchAccountByIban(dto.targetAccount)).thenReturn(target);
        when(userService.getUserById(dto.initiatedBy)).thenReturn(user);

        Transaction transaction = mapper.toEntity(dto);

        assertEquals(Status.FAILED, transaction.getStatus());
        verify(accountService, never()).updateBalance(any(), any(), any());
    }
}

