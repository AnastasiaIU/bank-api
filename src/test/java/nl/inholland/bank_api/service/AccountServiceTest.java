package nl.inholland.bank_api.service;

import jakarta.persistence.EntityNotFoundException;
import nl.inholland.bank_api.constant.ErrorMessages;
import nl.inholland.bank_api.mapper.AccountMapper;
import nl.inholland.bank_api.model.dto.AccountWithUserDTO;
import nl.inholland.bank_api.model.dto.UpdateAccountLimitsDTO;
import nl.inholland.bank_api.model.entities.Account;
import nl.inholland.bank_api.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;

@ExtendWith(SpringExtension.class)
@Import(AccountService.class)
public class AccountServiceTest {
    @Autowired
    private AccountService accountService;

    @MockitoBean
    private AccountRepository accountRepository;

    @MockitoBean
    private AccountMapper mapper;

    @Test
    void fetchAllAccountsSucceeds() {
        Pageable pageable = PageRequest.of(0, 2);

        // Prepare sample accounts
        Account account1 = new Account();
        account1.setIban("NL01INHO0000000001");
        Account account2 = new Account();
        account2.setIban("NL02INHO0000000002");

        List<Account> accountsList = List.of(account1, account2);
        Page<Account> accountsPage = new PageImpl<>(accountsList, pageable, accountsList.size());

        // Prepare corresponding DTOs
        AccountWithUserDTO dto1 = new AccountWithUserDTO();
        AccountWithUserDTO dto2 = new AccountWithUserDTO();

        // Mock repository and mapper
        when(accountRepository.findAll(pageable)).thenReturn(accountsPage);
        when(mapper.toAccountWithUserDTO(account1)).thenReturn(dto1);
        when(mapper.toAccountWithUserDTO(account2)).thenReturn(dto2);

        // Call service method
        Page<AccountWithUserDTO> result = accountService.fetchAllAccounts(pageable);

        // Verify repository interaction
        verify(accountRepository).findAll(pageable);

        // Assert the page content is correctly mapped
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(dto1, result.getContent().get(0));
        assertEquals(dto2, result.getContent().get(1));

        // Assert page metadata matches
        assertEquals(accountsPage.getTotalElements(), result.getTotalElements());
        assertEquals(accountsPage.getNumber(), result.getNumber());
        assertEquals(accountsPage.getSize(), result.getSize());
    }

    @Test
    void updateAccountLimitsThrowsWhenIbanNotFound() {
        String iban = "NONEXISTENT_IBAN";
        UpdateAccountLimitsDTO dto = new UpdateAccountLimitsDTO();

        when(accountRepository.findByIban(iban)).thenReturn(Optional.empty());

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            accountService.updateAccountLimits(iban, dto);
        });

        assertTrue(thrown.getMessage().contains(ErrorMessages.ACCOUNT_NOT_FOUND));
        verify(accountRepository, never()).save(any());
    }

    @Test
    void updateAccountLimitsSucceeds() {
        String iban = "NL12INHO0000000001";
        UpdateAccountLimitsDTO dto = new UpdateAccountLimitsDTO();
        dto.setDailyLimit(new BigDecimal("1000"));
        dto.setAbsoluteLimit(new BigDecimal("-400"));
        dto.setWithdrawLimit(new BigDecimal("300"));

        Account account = new Account();
        account.setIban(iban);
        account.setDailyLimit(BigDecimal.ZERO);
        account.setAbsoluteLimit(BigDecimal.ZERO);
        account.setWithdrawLimit(BigDecimal.ZERO);

        // Mock repository behavior
        when(accountRepository.findByIban(iban)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        // Call the service method
        accountService.updateAccountLimits(iban, dto);

        // Verify the account limits are updated
        assertEquals(dto.getDailyLimit(), account.getDailyLimit());
        assertEquals(dto.getAbsoluteLimit(), account.getAbsoluteLimit());
        assertEquals(dto.getWithdrawLimit(), account.getWithdrawLimit());

        // Verify that the save method was called on the repository with the updated account
        verify(accountRepository).save(account);
    }
}
