package nl.inholland.bank_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.inholland.bank_api.model.dto.AccountWithUserDTO;
import nl.inholland.bank_api.model.dto.ApprovalStatusUpdateDTO;
import nl.inholland.bank_api.model.dto.UserProfileDTO;
import nl.inholland.bank_api.model.enums.UserAccountStatus;
import nl.inholland.bank_api.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(SpringExtension.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getPendingUsers_ReturnsList() throws Exception {
        UserProfileDTO dto = new UserProfileDTO(1L, "Alice", "Smith", "alice@mail.com", "123456789", "+31612345678", UserAccountStatus.PENDING, null);

        when(userService.getPendingUsers()).thenReturn(List.of(dto));

        mockMvc.perform(get("/users/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void getUser_ReturnsUserProfile() throws Exception {
        UserProfileDTO dto = new UserProfileDTO(2L, "Bob", "Brown", "bob@mail.com", "987654321", "+31600000000", UserAccountStatus.APPROVED, null);

        when(userService.getProfileById(2L)).thenReturn(dto);

        mockMvc.perform(get("/users/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L));
    }

    @Test
    void getActiveUsers_ReturnsList() throws Exception {
        UserProfileDTO dto = new UserProfileDTO(3L, "Charlie", "Green", "charlie@mail.com", "111111111", "+31622222222", UserAccountStatus.APPROVED, null);

        when(userService.getActiveUsers()).thenReturn(List.of(dto));

        mockMvc.perform(get("/users/accounts/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3L));
    }

    @Test
    void updateApprovalStatus_Returns204() throws Exception {
        ApprovalStatusUpdateDTO updateDTO = new ApprovalStatusUpdateDTO();
        updateDTO.setUserAccountStatus(UserAccountStatus.APPROVED);

        doNothing().when(userService).updateApprovalStatus(5L, UserAccountStatus.APPROVED);

        mockMvc.perform(put("/users/5/approval-status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNoContent());

        verify(userService).updateApprovalStatus(5L, UserAccountStatus.APPROVED);
    }

    @Test
    void createDefaultAccounts_Returns201() throws Exception {
        AccountWithUserDTO dto = new AccountWithUserDTO();
        dto.setIban("NL00INHO0000000006");

        mockMvc.perform(post("/users/7/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(dto))))
                .andExpect(status().isCreated());

        verify(userService).createAccountsForUser(eq(7L), any());
    }

    @Test
    void closeUserWithAccounts_Returns204() throws Exception {
        doNothing().when(userService).closeUserAndAccounts(9L);

        mockMvc.perform(put("/users/9/close"))
                .andExpect(status().isNoContent());

        verify(userService).closeUserAndAccounts(9L);
    }
}