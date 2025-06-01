package nl.inholland.bank_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.inholland.bank_api.constant.ErrorMessages;
import nl.inholland.bank_api.constant.FieldNames;
import nl.inholland.bank_api.exception.GlobalExceptionHandler;
import nl.inholland.bank_api.model.dto.RegisterRequestDTO;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtUtil jwtUtil;

    private RegisterRequestDTO getValidRegisterRequest() {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.firstName = "Jane";
        dto.lastName = "Doe";
        dto.email = "jane.doe@example.com";
        dto.password = "Password123!";
        dto.bsn = "123456789";
        dto.phoneNumber = "+31612345678";
        return dto;
    }

    @Test
    void registerReturns201WithId() throws Exception {
        RegisterRequestDTO request = getValidRegisterRequest();

        // Mock the userService to return a user ID when createUser is called
        when(userService.createUser(any(RegisterRequestDTO.class))).thenReturn(1L);

        // Perform the mock POST request to register a new user
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));

        // Verify that the userService's createUser method was called ones
        verify(userService, times(1)).createUser(any(RegisterRequestDTO.class));
    }

    @Test
    void registerValidationErrorsReturn400() throws Exception {
        RegisterRequestDTO request = getValidRegisterRequest();
        request.firstName = "";

        // Perform the mock POST request to register a new user with missing firstName
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", hasItem(
                        StringUtils.fieldError(FieldNames.FIRST_NAME, ErrorMessages.FIRST_NAME_REQUIRED)
                )));

        // Verify that the userService's createUser method was never called
        verify(userService, never()).createUser(any());
    }

    @Test
    void registerReturns400WhenServiceThrowsException() throws Exception {
        RegisterRequestDTO request = getValidRegisterRequest();

        // Mock the userService to throw an IllegalArgumentException when createUser is called
        when(userService.createUser(any(RegisterRequestDTO.class)))
                .thenThrow(new IllegalArgumentException(
                        StringUtils.fieldError(FieldNames.EMAIL, ErrorMessages.EMAIL_EXISTS)
                ));

        // Perform the mock POST request to register a new user with an existing email
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", hasItem(
                        StringUtils.fieldError(FieldNames.EMAIL, ErrorMessages.EMAIL_EXISTS)
                )));
    }
}
