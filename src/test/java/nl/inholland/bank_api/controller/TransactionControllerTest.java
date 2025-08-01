package nl.inholland.bank_api.controller;

import nl.inholland.bank_api.model.dto.TransactionRequestDTO;
import nl.inholland.bank_api.service.TransactionService;
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

    @Test
    void postTransaction_ReturnsCreatedId() throws Exception {
        TransactionRequestDTO dto = new TransactionRequestDTO();
        dto.setSourceAccount("NL91ABNA0417164300");
        dto.setTargetAccount("NL91ABNA0417164301");
        dto.setInitiatedBy(1L);
        dto.setAmount(new BigDecimal("250.00"));
        dto.setDescription("Monthly savings");

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