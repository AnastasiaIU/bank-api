package nl.inholland.bank_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import nl.inholland.bank_api.constant.ErrorMessages;
import nl.inholland.bank_api.constant.FieldNames;
import nl.inholland.bank_api.exception.GlobalExceptionHandler;
import nl.inholland.bank_api.model.dto.AtmTransactionDTO;
import nl.inholland.bank_api.model.dto.AtmTransactionRequestDTO;
import nl.inholland.bank_api.model.entities.Account;
import nl.inholland.bank_api.model.entities.User;
import nl.inholland.bank_api.model.enums.AtmTransactionType;
import nl.inholland.bank_api.service.AccountService;
import nl.inholland.bank_api.service.AtmTransactionService;
import nl.inholland.bank_api.service.UserService;
import nl.inholland.bank_api.util.JwtUtil;
import nl.inholland.bank_api.util.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(AtmTransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AtmTransactionControllerTest {
    private final String ATM_TRANSACTIONS_ENDPOINT = "/atm/transactions";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AtmTransactionService atmTransactionService;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtUtil jwtUtil;

    private AtmTransactionRequestDTO getValidRequest(AtmTransactionType type, BigDecimal amount) {
        AtmTransactionRequestDTO dto = new AtmTransactionRequestDTO();
        dto.iban = "NL01INHO0123456789";
        dto.type = type;
        dto.amount = amount;
        return dto;
    }

    private AtmTransactionDTO getValidResponse(AtmTransactionRequestDTO request) {
        return new AtmTransactionDTO(
                5L,
                request.iban,
                1L,
                request.type,
                request.amount,
                LocalDateTime.now(),
                "PENDING",
                null
        );
    }

    private Authentication auth() {
        return new UsernamePasswordAuthenticationToken("john.doe@example.com", null);
    }

    @Test
    void createTransactionReturns201WithDto() throws Exception {
        AtmTransactionRequestDTO request = getValidRequest(AtmTransactionType.DEPOSIT, new BigDecimal("50.00"));
        AtmTransactionDTO response = getValidResponse(request);

        // Mock the necessary services
        when(userService.getUserByEmail("john.doe@example.com")).thenReturn(new User());
        when(accountService.fetchAccountByIban(request.iban)).thenReturn(new Account());
        when(atmTransactionService
                .createTransaction(any(AtmTransactionRequestDTO.class), any(Account.class), any(User.class)))
                .thenReturn(response);

        // Perform the request and verify the response
        mockMvc.perform(post(ATM_TRANSACTIONS_ENDPOINT)
                        .principal(auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5L));

        // Verify that the service was called with the correct parameters
        verify(atmTransactionService)
                .createTransaction(any(AtmTransactionRequestDTO.class), any(Account.class), any(User.class));
    }

    @Test
    void createTransactionValidationErrorsReturn400ForEmptyDtoFields() throws Exception {
        AtmTransactionRequestDTO request = new AtmTransactionRequestDTO();
        request.iban = "";
        request.type = null;
        request.amount = null;

        // Perform the request and verify the response
        mockMvc.perform(post(ATM_TRANSACTIONS_ENDPOINT)
                        .principal(auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", hasItems(
                        StringUtils.fieldError(FieldNames.IBAN, ErrorMessages.IBAN_REQUIRED),
                        StringUtils.fieldError(FieldNames.TYPE, ErrorMessages.TRANSACTION_TYPE_REQUIRED),
                        StringUtils.fieldError(FieldNames.AMOUNT, ErrorMessages.AMOUNT_REQUIRED)
                )));
    }

    @Test
    void createTransactionValidationErrorsReturn400ForInvalidDtoFields() throws Exception {
        AtmTransactionRequestDTO request = new AtmTransactionRequestDTO();
        request.iban = "123";
        request.amount = new BigDecimal("-50.00");

        // Perform the request and verify the response
        mockMvc.perform(post(ATM_TRANSACTIONS_ENDPOINT)
                        .principal(auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", hasItems(
                        StringUtils.fieldError(FieldNames.IBAN, ErrorMessages.INVALID_IBAN_FORMAT),
                        StringUtils.fieldError(FieldNames.AMOUNT, ErrorMessages.AMOUNT_MINIMUM)
                )));
    }

    @Test
    void createTransactionReturns400WhenInsufficientBalance() throws Exception {
        AtmTransactionRequestDTO request = getValidRequest(AtmTransactionType.WITHDRAW, new BigDecimal("50.00"));

        // Mock the necessary services
        when(userService.getUserByEmail("john.doe@example.com")).thenReturn(new User());
        when(accountService.fetchAccountByIban(request.iban)).thenReturn(new Account());
        when(atmTransactionService
                .createTransaction(any(AtmTransactionRequestDTO.class), any(Account.class), any(User.class)))
                .thenThrow(new IllegalArgumentException(ErrorMessages.INSUFFICIENT_BALANCE));

        // Perform the request and verify the response
        mockMvc.perform(post(ATM_TRANSACTIONS_ENDPOINT)
                        .principal(auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", hasItem(ErrorMessages.INSUFFICIENT_BALANCE)));
    }

    @Test
    void createTransactionReturns404WhenUserNotFound() throws Exception {
        AtmTransactionRequestDTO request = getValidRequest(AtmTransactionType.DEPOSIT, new BigDecimal("50.00"));

        // Mock the userService to throw the UsernameNotFoundException
        when(userService.getUserByEmail("john.doe@example.com"))
                .thenThrow(new UsernameNotFoundException(ErrorMessages.USER_NOT_FOUND));

        // Perform the request and verify the response
        mockMvc.perform(post(ATM_TRANSACTIONS_ENDPOINT)
                        .principal(auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", hasItem(ErrorMessages.USER_NOT_FOUND)));
    }

    @Test
    void getTransactionReturns200WithDto() throws Exception {
        AtmTransactionDTO response = getValidResponse(
                getValidRequest(AtmTransactionType.DEPOSIT, new BigDecimal("50.00"))
        );

        // Mock the AtmTransactionService to return the response
        when(atmTransactionService.getTransaction(5L)).thenReturn(response);

        // Perform the request and verify the response
        mockMvc.perform(get(ATM_TRANSACTIONS_ENDPOINT + "/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L));

        // Verify that the service was called with the correct ID
        verify(atmTransactionService).getTransaction(5L);
    }

    @Test
    void getTransactionReturns404WhenServiceThrowsException() throws Exception {
        // Mock the AtmTransactionService to throw an exception
        when(atmTransactionService.getTransaction(5L))
                .thenThrow(new EntityNotFoundException(ErrorMessages.TRANSACTION_NOT_FOUND));

        // Perform the request and verify the response
        mockMvc.perform(get(ATM_TRANSACTIONS_ENDPOINT + "/5"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", hasItem(ErrorMessages.TRANSACTION_NOT_FOUND)));
    }
}