package nl.inholland.bank_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.inholland.bank_api.constant.ErrorMessages;
import nl.inholland.bank_api.exception.GlobalExceptionHandler;
import nl.inholland.bank_api.model.dto.CombinedTransactionDTO;
import nl.inholland.bank_api.model.dto.CombinedTransactionFullHistoryDTO;
import nl.inholland.bank_api.model.dto.TransactionFilterDTO;
import nl.inholland.bank_api.model.entities.User;
import nl.inholland.bank_api.model.enums.Status;
import nl.inholland.bank_api.model.enums.UserRole;
import nl.inholland.bank_api.service.AccountService;
import nl.inholland.bank_api.service.CombinedTransactionService;
import nl.inholland.bank_api.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(CombinedTransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
public class CombinedTransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CombinedTransactionService combinedTransactionService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AccountService accountService;

    private Authentication auth(String email) {
        return new UsernamePasswordAuthenticationToken(email, null);
    }

    @Test
    void getTransactionsAsEmployeeReturns200() throws Exception {
        String email = "employee@bank.com";
        User user = new User();
        user.setEmail(email);
        user.setRole(UserRole.EMPLOYEE);

        CombinedTransactionDTO dto = new CombinedTransactionDTO();
        Page<CombinedTransactionDTO> resultPage = new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1);

        when(userService.getUserByEmail(email)).thenReturn(user);
        when(combinedTransactionService.getFilteredTransactions(eq(1L), any(TransactionFilterDTO.class), any(Pageable.class)))
                .thenReturn(resultPage);

        mockMvc.perform(get("/accounts/1/transactions")
                        .principal(auth(email))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getTransactionsAsUnauthorizedUserReturns403() throws Exception {
        String email = "user@bank.com";
        User user = new User();
        user.setEmail(email);
        user.setRole(UserRole.CUSTOMER);

        when(userService.getUserByEmail(email)).thenReturn(user);
        when(accountService.ownsAccount(user.getId(), 1L)).thenReturn(false);

        mockMvc.perform(get("/accounts/1/transactions")
                        .principal(auth(email))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", hasItem(ErrorMessages.ACCESS_DENIED)));
    }

    @Test
    void customerGetsTransactionsWithDescriptionFilter() throws Exception {
        CombinedTransactionDTO dto = new CombinedTransactionDTO();
        dto.description = "Rent";

        User customer = new User();
        customer.setId(42L);
        customer.setEmail("user@mail.com");
        customer.setRole(UserRole.CUSTOMER);

        when(userService.getUserByEmail("user@mail.com")).thenReturn(customer);
        when(accountService.ownsAccount(42L, 99L)).thenReturn(true); // user owns account 99
        when(combinedTransactionService.getFilteredTransactions(eq(99L), any(), any())).thenReturn(
                new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1)
        );

        Authentication auth = new UsernamePasswordAuthenticationToken("user@mail.com", null);

        mockMvc.perform(get("/accounts/99/transactions")
                        .principal(auth)
                        .param("description", "Rent")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].description").value("Rent"));
    }

    @Test
    void getAllTransactionsForAccountReturnsPaginatedTransactions() throws Exception {
        Authentication auth = new UsernamePasswordAuthenticationToken("user@mail.com", null);

        User user = new User();
        user.setId(1L);
        user.setEmail("user@mail.com");
        user.setRole(UserRole.CUSTOMER);

        CombinedTransactionDTO dto1 = new CombinedTransactionDTO();
        dto1.id = 1L;
        CombinedTransactionDTO dto2 = new CombinedTransactionDTO();
        dto2.id = 2L;

        when(userService.getUserByEmail("user@mail.com")).thenReturn(user);
        when(accountService.ownsAccount(1L, 1L)).thenReturn(true);
        when(combinedTransactionService.getFilteredTransactions(eq(1L), any(), any()))
                .thenReturn(new PageImpl<>(List.of(dto1, dto2), PageRequest.of(0, 2), 2));

        mockMvc.perform(get("/accounts/1/transactions")
                        .principal(auth)
                        .param("page", "0")
                        .param("size", "2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[1].id").value(2));
    }

    @Test
    void getAllCombinedTransactions_ReturnsPaginatedTransactions() throws Exception {
        CombinedTransactionFullHistoryDTO tx1 = new CombinedTransactionFullHistoryDTO(
                "NL91ABNA0417164300",
                "NL91ABNA0417164301",
                1L,
                new BigDecimal("250.00"),
                LocalDateTime.parse("2025-06-09T14:20:00"),
                "TRANSFER",
                Status.SUCCEEDED
        );

        CombinedTransactionFullHistoryDTO tx2 = new CombinedTransactionFullHistoryDTO(
                "NL91ABNA0417164300",
                null,
                1L,
                new BigDecimal("100.00"),
                LocalDateTime.parse("2025-06-09T13:10:00"),
                "WITHDRAW",
                Status.SUCCEEDED
        );

        List<CombinedTransactionFullHistoryDTO> content = List.of(tx1, tx2);
        Page<CombinedTransactionFullHistoryDTO> page = new PageImpl<>(content, PageRequest.of(0, 10), 2);

        when(combinedTransactionService.getAllCombinedTransactions(PageRequest.of(0, 10))).thenReturn(page);

        mockMvc.perform(get("/combined-transactions")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].sourceIban").value("NL91ABNA0417164300"))
                .andExpect(jsonPath("$.content[0].targetIban").value("NL91ABNA0417164301"))
                .andExpect(jsonPath("$.content[0].initiatedBy").value(1))
                .andExpect(jsonPath("$.content[0].amount").value(250.00))
                .andExpect(jsonPath("$.content[0].type").value("TRANSFER"))
                .andExpect(jsonPath("$.content[0].status").value("SUCCEEDED"))
                .andExpect(jsonPath("$.content[1].type").value("WITHDRAW"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1));
    }
}