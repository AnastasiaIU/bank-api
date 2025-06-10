package nl.inholland.bank_api.controller;

import nl.inholland.bank_api.model.dto.TransactionRequestDTO;
import nl.inholland.bank_api.service.AccountService;
import nl.inholland.bank_api.service.CombinedTransactionService;
import nl.inholland.bank_api.service.TransactionService;
import nl.inholland.bank_api.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransactionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService transactionService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private CombinedTransactionService combinedTransactionService;

    @Test
    void postTransaction_ReturnsCreatedId() throws Exception {
        TransactionRequestDTO dto = new TransactionRequestDTO();
        dto.sourceAccount = "NL91ABNA0417164300";
        dto.targetAccount = "NL91ABNA0417164301";
        dto.initiatedBy = 1L;
        dto.amount = new BigDecimal("250.00");
        dto.description = "Monthly savings";

        when(transactionService.postTransaction(any(TransactionRequestDTO.class))).thenReturn(42L);

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "sourceAccount": "NL91ABNA0417164300",
                      "targetAccount": "NL91ABNA0417164301",
                      "initiatedBy": 1,
                      "amount": 250.00,
                      "description": "Monthly savings"
                    }
                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(42));
    }
}