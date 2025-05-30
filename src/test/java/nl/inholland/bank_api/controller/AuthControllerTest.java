package nl.inholland.bank_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.inholland.bank_api.model.dto.RegisterRequestDTO;
import nl.inholland.bank_api.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void register_ShouldReturn201_WhenRegistrationIsSuccessful() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.firstName = "John";
        request.lastName = "Doe";
        request.email = "john.doe@example.com";
        request.password = "password";
        request.bsn = "123456789";
        request.phoneNumber = "+31612345678";

        when(userService.createUser(any(RegisterRequestDTO.class))).thenReturn(1L);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));

        verify(userService, times(1)).createUser(any(RegisterRequestDTO.class));
    }

    @Test
    void register_ShouldReturn400_WhenRequestIsInvalid() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.lastName = "Doe";
        request.email = "john.doe@example.com";
        request.password = "password";
        request.bsn = "123456789";
        request.phoneNumber = "+31612345678";

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.exception").value("MethodArgumentNotValidException"))
                .andExpect(jsonPath("$.message[0]").value("firstName: First name is required"));

        verify(userService, never()).createUser(any());
    }
}
