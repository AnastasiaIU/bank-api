package nl.inholland.bank_api.mapper;

import nl.inholland.bank_api.model.dto.AtmTransactionDTO;
import nl.inholland.bank_api.model.dto.AtmTransactionRequestDTO;
import nl.inholland.bank_api.model.dto.CombinedTransactionDTO;
import nl.inholland.bank_api.model.entities.Account;
import nl.inholland.bank_api.model.entities.AtmTransaction;
import nl.inholland.bank_api.model.entities.User;
import nl.inholland.bank_api.model.enums.AtmTransactionType;
import nl.inholland.bank_api.model.enums.Status;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AtmTransactionMapperTest {
    private final AtmTransactionMapper mapper = new AtmTransactionMapper();

    private AtmTransactionRequestDTO getValidRequest(AtmTransactionType type, BigDecimal amount) {
        AtmTransactionRequestDTO dto = new AtmTransactionRequestDTO();
        dto.iban = "NL01INHO0123456789";
        dto.type = type;
        dto.amount = amount;
        return dto;
    }

    @Test
    void toEntityMapsFieldsCorrectly() {
        AtmTransactionRequestDTO dto = getValidRequest(AtmTransactionType.DEPOSIT, BigDecimal.TEN);
        Account account = new Account();
        User user = new User();
        AtmTransaction entity = mapper.toEntity(dto, account, user);

        // Verify that the fields are mapped correctly
        assertEquals(account, entity.getAccount());
        assertEquals(user, entity.getInitiatedBy());
        assertEquals(dto.type, entity.getType());
        assertEquals(dto.amount, entity.getAmount());
        assertEquals(Status.PENDING, entity.getStatus());
    }

    @Test
    void toAtmTransactionDTOReturnsAllFields() {
        Account account = new Account();
        account.setIban("NL01INHO0123456789");
        User user = new User();
        user.setId(5L);
        AtmTransaction entity = AtmTransaction.builder()
                .id(1L)
                .account(account)
                .initiatedBy(user)
                .type(AtmTransactionType.WITHDRAW)
                .amount(new BigDecimal("50.00"))
                .timestamp(LocalDateTime.now())
                .status(Status.SUCCEEDED)
                .failureReason(null)
                .build();
        AtmTransactionDTO dto = mapper.toAtmTransactionDTO(entity);

        // Verify that all fields are mapped correctly
        assertEquals(entity.getId(), dto.id());
        assertEquals(account.getIban(), dto.iban());
        assertEquals(user.getId(), dto.initiatedBy());
        assertEquals(entity.getType(), dto.type());
        assertEquals(entity.getAmount(), dto.amount());
        assertEquals(entity.getTimestamp(), dto.timestamp());
        assertEquals(entity.getStatus().name(), dto.status());
        assertNull(dto.failureReason());
    }

    @Test
    void toCombinedDTO_MapsWithdrawFieldsCorrectly() {
        Account account = new Account();
        account.setIban("NL01INHO0000000001");

        AtmTransaction transaction = AtmTransaction.builder()
                .id(10L)
                .account(account)
                .type(AtmTransactionType.WITHDRAW)
                .amount(BigDecimal.valueOf(100))
                .timestamp(LocalDateTime.now())
                .status(Status.SUCCEEDED)
                .failureReason(null)
                .build();

        CombinedTransactionDTO dto = mapper.toCombinedDTO(transaction);

        assertEquals(10L, dto.id);
        assertEquals("ATM", dto.type);
        assertEquals("NL01INHO0000000001", dto.sourceIban);
        assertNull(dto.targetIban);
        assertEquals(transaction.getAmount(), dto.amount);
        assertEquals(transaction.getTimestamp(), dto.timestamp);
        assertEquals(transaction.getStatus(), dto.status);
        assertNull(dto.failureReason);
        assertEquals("WITHDRAW", dto.description);
    }

    @Test
    void toCombinedDTO_MapsDepositFieldsCorrectly() {
        Account account = new Account();
        account.setIban("NL01INHO0000000002");

        AtmTransaction transaction = AtmTransaction.builder()
                .id(11L)
                .account(account)
                .type(AtmTransactionType.DEPOSIT)
                .amount(BigDecimal.valueOf(250))
                .timestamp(LocalDateTime.now())
                .status(Status.SUCCEEDED)
                .failureReason("None")
                .build();

        CombinedTransactionDTO dto = mapper.toCombinedDTO(transaction);

        assertEquals(11L, dto.id);
        assertEquals("ATM", dto.type);
        assertNull(dto.sourceIban);
        assertEquals("NL01INHO0000000002", dto.targetIban);
        assertEquals(transaction.getAmount(), dto.amount);
        assertEquals(transaction.getTimestamp(), dto.timestamp);
        assertEquals(transaction.getStatus(), dto.status);
        assertEquals("None", dto.failureReason);
        assertEquals("DEPOSIT", dto.description);
    }
}