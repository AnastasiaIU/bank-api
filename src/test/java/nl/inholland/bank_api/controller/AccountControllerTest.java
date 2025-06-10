package nl.inholland.bank_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.inholland.bank_api.constant.ErrorMessages;
import nl.inholland.bank_api.model.dto.AccountDTO;
import nl.inholland.bank_api.model.dto.AccountWithUserDTO;
import nl.inholland.bank_api.model.dto.UpdateAccountLimitsDTO;
import nl.inholland.bank_api.service.AccountService;
import nl.inholland.bank_api.util.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AccountControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fetchAllAccountsReturnsPaginatedAccounts() throws Exception {
        AccountWithUserDTO account1 = new AccountWithUserDTO();
        account1.iban = "NL91ABNA0417164300";
        account1.type = "CHECKING";
        account1.balance = BigDecimal.valueOf(10000);
        account1.firstName = "John";
        account1.lastName = "Doe";
        account1.dailyLimit = BigDecimal.valueOf(5000);
        account1.absoluteLimit = BigDecimal.valueOf(-300);
        account1.withdrawLimit = BigDecimal.valueOf(3000);

        AccountWithUserDTO account2 = new AccountWithUserDTO();
        account2.iban = "NL91ABNA0417164301";
        account2.type = "SAVINGS";
        account2.balance = BigDecimal.valueOf(50000);
        account2.firstName = "John";
        account2.lastName = "Doe";
        account2.dailyLimit = BigDecimal.valueOf(5000);
        account2.absoluteLimit = BigDecimal.valueOf(-100);
        account2.withdrawLimit = BigDecimal.valueOf(1000);

        Page<AccountWithUserDTO> page = new PageImpl<>(List.of(account1, account2), PageRequest.of(0, 10), 2);

        when(accountService.fetchAllAccounts(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/accounts")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].iban").value("NL91ABNA0417164300"))
                .andExpect(jsonPath("$.content[1].iban").value("NL91ABNA0417164301"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    void updateAccountLimitsSuccess() throws Exception {
        String iban = "NL91ABNA0417164300";

        UpdateAccountLimitsDTO dto = new UpdateAccountLimitsDTO();
        dto.setDailyLimit(BigDecimal.valueOf(6000));
        dto.setAbsoluteLimit(BigDecimal.valueOf(-500));
        dto.setWithdrawLimit(BigDecimal.valueOf(3500));

        // Mock service method call to do nothing (void)
        doNothing().when(accountService).updateAccountLimits(eq(iban), eq(dto));

        // Convert DTO to JSON
        String jsonRequest = objectMapper.writeValueAsString(dto);

        mockMvc.perform(put("/accounts/{iban}/limits", iban)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk());

        // Verify service method was called correctly
        verify(accountService).updateAccountLimits(eq(iban), eq(dto));
    }

    @Test
    void updateAccountLimitsReturn400ForInvalidLimits() throws Exception {
        String iban = "NL91ABNA0417164300";

        UpdateAccountLimitsDTO invalidDto = new UpdateAccountLimitsDTO();
        invalidDto.setDailyLimit(new BigDecimal("-100")); // invalid, less than 0
        invalidDto.setWithdrawLimit(new BigDecimal("-100")); // invalid, less than 0
        invalidDto.setAbsoluteLimit(new BigDecimal("-500"));

        mockMvc.perform(put("/accounts/{iban}/limits", iban)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", hasItems(
                        StringUtils.fieldError("dailyLimit", ErrorMessages.DAILY_LIMIT_MINIMUM),
                        StringUtils.fieldError("withdrawLimit", ErrorMessages.WITHDRAW_LIMIT_MINIMUM))));
    }

    @Test
    void fetchAccountByIban_ReturnsAccountDTO() throws Exception {
        String iban = "NL91ABNA0417164300";
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setIban(iban);
        accountDTO.setType("CHECKING");
        accountDTO.setStatus("ACTIVE");
        accountDTO.setBalance(new BigDecimal("10000.00"));

        when(accountService.fetchAccountDTOByIban(iban)).thenReturn(accountDTO);

        mockMvc.perform(get("/accounts/{iban}", iban)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.iban").value(iban))
                .andExpect(jsonPath("$.type").value("CHECKING"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.balance").value(10000.00));
    }

    @Test
    void fetchCheckingAccountsByUserId_ReturnsListOfAccountDTO() throws Exception {
        Long userId = 1L;
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setIban("NL91ABNA0417164300");
        accountDTO.setType("CHECKING");
        accountDTO.setStatus("ACTIVE");
        accountDTO.setBalance(new BigDecimal("10000.00"));

        when(accountService.fetchCheckingAccountsByUserId(userId)).thenReturn(List.of(accountDTO));

        mockMvc.perform(get("/users/{userId}/checking-accounts", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].iban").value("NL91ABNA0417164300"))
                .andExpect(jsonPath("$[0].type").value("CHECKING"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[0].balance").value(10000.00));
    }

    @Test
    void fetchAccountsByUserId_ReturnsListOfAccountDTOs() throws Exception {
        AccountDTO dto1 = new AccountDTO();
        dto1.setIban("NL01INHO0000000001");
        dto1.setStatus("ACTIVE");
        dto1.setType("CHECKING");
        dto1.setBalance(BigDecimal.valueOf(1000));

        AccountDTO dto2 = new AccountDTO();
        dto2.setIban("NL02INHO0000000002");
        dto2.setStatus("ACTIVE");
        dto2.setType("SAVINGS");
        dto2.setBalance(BigDecimal.valueOf(5000));

        when(accountService.fetchAccountsByUserId(1L)).thenReturn(List.of(dto1, dto2));

        mockMvc.perform(get("/users/1/accounts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].iban").value("NL01INHO0000000001"))
                .andExpect(jsonPath("$[1].iban").value("NL02INHO0000000002"));
    }

    @Test
    void createDefaultAccounts_ReturnsTwoAccountsWithUserInfo() throws Exception {
        AccountWithUserDTO acc1 = new AccountWithUserDTO();
        acc1.iban = "NL91ABNA0417164300";
        acc1.firstName = "Alice";
        acc1.lastName = "Smith";
        acc1.type = "CHECKING";
        acc1.balance = BigDecimal.ZERO;

        AccountWithUserDTO acc2 = new AccountWithUserDTO();
        acc2.iban = "NL91ABNA0417164301";
        acc2.firstName = "Alice";
        acc2.lastName = "Smith";
        acc2.type = "SAVINGS";
        acc2.balance = BigDecimal.ZERO;

        when(accountService.createAccountsByUserId(1L)).thenReturn(List.of(acc1, acc2));

        mockMvc.perform(get("/users/1/accounts/review")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].iban").value("NL91ABNA0417164300"))
                .andExpect(jsonPath("$[1].iban").value("NL91ABNA0417164301"))
                .andExpect(jsonPath("$[0].type").value("CHECKING"))
                .andExpect(jsonPath("$[1].type").value("SAVINGS"));
    }

    @Test
    void closeAccountByIban_ReturnsNoContent() throws Exception {
        String iban = "NL91ABNA0417164300";

        doNothing().when(accountService).closeAccountByIban(iban);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/accounts/{iban}/close", iban))
                .andExpect(status().isNoContent());

        verify(accountService).closeAccountByIban(iban);
    }
    @Test
    void fetchAccountsByName_ReturnsListOfAccountDTO() throws Exception {
        String firstName = "John";
        String lastName = "Doe";
        Long id = 2L;

        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setIban("NL91ABNA0417164309");
        accountDTO.setType("CHECKING");
        accountDTO.setStatus("ACTIVE");
        accountDTO.setBalance(new BigDecimal("8700.00"));

        when(accountService.fetchAccountsByName(firstName, lastName, id)).thenReturn(List.of(accountDTO));

        mockMvc.perform(get("/users/accounts/{firstName}/{lastName}/{id}", firstName, lastName, id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].iban").value("NL91ABNA0417164309"))
                .andExpect(jsonPath("$[0].type").value("CHECKING"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[0].balance").value(8700.00));
    }
}
