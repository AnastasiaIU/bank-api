package nl.inholland.bank_api.service;

import nl.inholland.bank_api.constant.ErrorMessages;
import nl.inholland.bank_api.constant.FieldNames;
import nl.inholland.bank_api.exception.ConflictException;
import nl.inholland.bank_api.mapper.UserMapper;
import nl.inholland.bank_api.model.dto.LoginResponseDTO;
import nl.inholland.bank_api.model.dto.RegisterRequestDTO;
import nl.inholland.bank_api.model.dto.LoginRequestDTO;
import nl.inholland.bank_api.model.entities.User;
import nl.inholland.bank_api.model.enums.UserAccountStatus;
import nl.inholland.bank_api.model.enums.UserRole;
import nl.inholland.bank_api.repository.UserRepository;
import nl.inholland.bank_api.util.JwtUtil;
import nl.inholland.bank_api.util.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.Optional;

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

    @Test
    void loginReturnsTokenWhenCredentialsAreCorrect() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.email = "jane.doe@example.com";
        request.password = "Password123!";

        User user = User.builder()
                .id(1L)
                .email("jane.doe@example.com")
                .password("encodedPassword")
                .role(UserRole.CUSTOMER)
                .build();

        when(userRepository.findByEmail(request.email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password, user.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken(user.getEmail(), user.getRole(), user.getId())).thenReturn("mocked-jwt-token");

        LoginResponseDTO response = userService.login(request);

        assertEquals("mocked-jwt-token", response.token());
        verify(userRepository).findByEmail(request.email);
        verify(passwordEncoder).matches(request.password, user.getPassword());
        verify(jwtUtil).generateToken(user.getEmail(), user.getRole(), user.getId());
    }

    @Test
    void loginThrowsWhenEmailNotFound() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.email = "wrong@example.com";
        request.password = "Password123!";
        when(userRepository.findByEmail(request.email)).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> userService.login(request));
        verify(userRepository).findByEmail(request.email);
        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtUtil, never()).generateToken(any(), any(), any());
    }

    @Test
    void loginThrowsWhenPasswordDoesNotMatch() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.email = "jane.doe@example.com";
        request.password = "Wrong123!";

        User user = User.builder()
                .id(1L)
                .email("jane.doe@example.com")
                .password("encodedPassword")
                .role(UserRole.CUSTOMER)
                .build();

        when(userRepository.findByEmail(request.email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password, user.getPassword())).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> userService.login(request));
        verify(userRepository).findByEmail(request.email);
        verify(passwordEncoder).matches(request.password, user.getPassword());
        verify(jwtUtil, never()).generateToken(any(), any(), any());
    }

    @Test
    void loginThrowsWhenUserIsClosedOrRejected() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.email = "jane.doe@example.com";
        request.password = "Password123!";

        User user = User.builder()
                .id(1L)
                .email("jane.doe@example.com")
                .password("encodedPassword")
                .role(UserRole.CUSTOMER)
                .isApproved(UserAccountStatus.CLOSED)
                .build();

        when(userRepository.findByEmail(request.email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password, user.getPassword())).thenReturn(true);

        assertThrows(DisabledException.class, () -> userService.login(request));
        verify(userRepository).findByEmail(request.email);
        verify(passwordEncoder).matches(request.password, user.getPassword());
        verify(jwtUtil, never()).generateToken(any(), any(), any());
    }

    @Test
    void shouldReturnUserWhenIdExists() {
        // Arrange
        Long userId = 1L;

        User expectedUser = User.builder()
                .id(userId)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("hashed_password")
                .bsn("123456789")
                .phoneNumber("0612345678")
                .isApproved(UserAccountStatus.APPROVED)
                .role(UserRole.CUSTOMER)
                .accounts(new ArrayList<>())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));

        // Act
        User result = userService.getUserById(userId);

        // Assert
        assertEquals(expectedUser, result);
        verify(userRepository).findById(userId);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // Arrange
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userService.getUserById(userId)
        );

        assertEquals("User not found with id: " + userId, exception.getMessage());
        verify(userRepository).findById(userId);
    }
}