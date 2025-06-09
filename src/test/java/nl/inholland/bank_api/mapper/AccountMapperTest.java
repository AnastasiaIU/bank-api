package nl.inholland.bank_api.mapper;
import nl.inholland.bank_api.model.dto.AccountWithUserDTO;
import nl.inholland.bank_api.model.entities.Account;
import nl.inholland.bank_api.model.entities.User;
import nl.inholland.bank_api.model.enums.AccountStatus;
import nl.inholland.bank_api.model.enums.AccountType;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
public class AccountMapperTest {

    private final AccountMapper mapper = new AccountMapper();

    @Test
    void toAccountWithUserDTOMapsAllFields() {
        User user = User.builder()
                .firstName("Jane")
                .lastName("Smith")
                .build();

        Account account = Account.builder()
                .id(10L)
                .iban("NL12BANK3456789012")
                .status(AccountStatus.ACTIVE)
                .type(AccountType.CHECKING)
                .balance(new BigDecimal("10000"))
                .dailyLimit(new BigDecimal("800"))
                .absoluteLimit(new BigDecimal("-100"))
                .withdrawLimit(new BigDecimal("100"))
                .user(user)
                .build();

        AccountWithUserDTO dto = mapper.toAccountWithUserDTO(account);

        assertEquals(account.getId(), dto.getId());
        assertEquals(account.getIban(), dto.getIban());
        assertEquals(account.getStatus().name(), dto.getStatus());
        assertEquals(account.getType().name(), dto.getType());
        assertEquals(account.getBalance(), dto.getBalance());
        assertEquals(account.getDailyLimit(), dto.getDailyLimit());
        assertEquals(account.getAbsoluteLimit(), dto.getAbsoluteLimit());
        assertEquals(account.getWithdrawLimit(), dto.getWithdrawLimit());
        assertEquals(user.getFirstName(), dto.getFirstName());
        assertEquals(user.getLastName(), dto.getLastName());
    }

    @Test
    void toAccountMapsAllFieldsCorrectly() {
        AccountWithUserDTO dto = new AccountWithUserDTO();
        dto.setIban("NL12BANK3456789012");
        dto.setStatus("ACTIVE");
        dto.setType("CHECKING");
        dto.setBalance(new BigDecimal("10000"));
        dto.setDailyLimit(new BigDecimal("800"));
        dto.setAbsoluteLimit(new BigDecimal("-100"));
        dto.setWithdrawLimit(new BigDecimal("100"));

        User user = User.builder()
                .firstName("Jane")
                .lastName("Smith")
                .build();

        AccountMapper mapper = new AccountMapper();
        Account account = mapper.toAccount(dto, user);

        assertEquals(dto.getIban(), account.getIban());
        assertEquals(AccountStatus.ACTIVE, account.getStatus());
        assertEquals(AccountType.CHECKING, account.getType());
        assertEquals(dto.getBalance(), account.getBalance());
        assertEquals(dto.getDailyLimit(), account.getDailyLimit());
        assertEquals(dto.getAbsoluteLimit(), account.getAbsoluteLimit());
        assertEquals(dto.getWithdrawLimit(), account.getWithdrawLimit());
        assertEquals(user, account.getUser());
    }
}
