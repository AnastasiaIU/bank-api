package nl.inholland.bank_api.mapper;

import nl.inholland.bank_api.model.dto.AtmTransactionDTO;
import nl.inholland.bank_api.model.dto.AtmTransactionRequestDTO;
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
        AtmTransactionRequestDTO dto = getValidRequest(AtmTransactionType.DEPOSIT, new BigDecimal("10.00"));
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
}