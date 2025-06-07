package nl.inholland.bank_api.service;

import nl.inholland.bank_api.constant.ErrorMessages;
import nl.inholland.bank_api.constant.FieldNames;
import nl.inholland.bank_api.exception.ConflictException;
import nl.inholland.bank_api.mapper.UserMapper;
import nl.inholland.bank_api.model.dto.RegisterRequestDTO;
import nl.inholland.bank_api.model.entities.User;
import nl.inholland.bank_api.repository.UserRepository;
import nl.inholland.bank_api.util.JwtUtil;
import nl.inholland.bank_api.util.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@Import(UserService.class)
class UserServiceTest {
    @Autowired
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private UserMapper userMapper;

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
    void createUserThrowsWhenEmailExists() {
        RegisterRequestDTO request = getValidRegisterRequest();

        // Mock the userRepository to return true when checking for existing email
        when(userRepository.existsByEmail(request.email)).thenReturn(true);

        // Call the createUser method and expect a ConflictException
        ConflictException e = assertThrows(
                ConflictException.class, () -> userService.createUser(request)
        );

        // Verify the exception message and interactions with the repository
        assertEquals(StringUtils.fieldError(FieldNames.EMAIL, ErrorMessages.EMAIL_EXISTS), e.getMessage());

        // Verify that the repository was called to check for existing email
        verify(userRepository).existsByEmail(request.email);

        // Verify that the repository was not called to save a new user
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUserThrowsWhenBsnExists() {
        RegisterRequestDTO request = getValidRegisterRequest();

        // Mock the userRepository to return true for existing BSN
        when(userRepository.existsByBsn(request.bsn)).thenReturn(true);

        // Call the createUser method and expect a ConflictException
        ConflictException e = assertThrows(
                ConflictException.class, () -> userService.createUser(request)
        );

        // Verify the exception message and interactions with the repository
        assertEquals(StringUtils.fieldError(FieldNames.BSN, ErrorMessages.BSN_EXISTS), e.getMessage());

        // Verify that the repository was called to check for existing BSN
        verify(userRepository).existsByBsn(request.bsn);

        // Verify that the repository was not called to save a new user
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUserSavesAndReturnsId() {
        RegisterRequestDTO request = getValidRegisterRequest();

        // Mock the userRepository to return false for existing email and BSN
        when(userRepository.existsByEmail(request.email)).thenReturn(false);
        when(userRepository.existsByBsn(request.bsn)).thenReturn(false);

        User mapped = User.builder().build();
        User saved = User.builder().id(42L).build();

        // Mock the userMapper to return a User entity and the userRepository to return a saved User
        when(userMapper.toEntity(request, passwordEncoder)).thenReturn(mapped);
        when(userRepository.save(mapped)).thenReturn(saved);

        Long id = userService.createUser(request);

        // Verify that the user was saved and the ID returned
        assertEquals(42L, id);

        // Verify interactions with the repository and mapper
        verify(userRepository).existsByEmail(request.email);
        verify(userRepository).existsByBsn(request.bsn);
        verify(userMapper).toEntity(request, passwordEncoder);
        verify(userRepository).save(mapped);
    }
}