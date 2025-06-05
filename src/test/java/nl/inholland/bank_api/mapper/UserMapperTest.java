package nl.inholland.bank_api.mapper;

import nl.inholland.bank_api.model.dto.RegisterRequestDTO;
import nl.inholland.bank_api.model.dto.UserProfileDTO;
import nl.inholland.bank_api.model.entities.User;
import nl.inholland.bank_api.model.enums.UserAccountStatus;
import nl.inholland.bank_api.model.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserMapperTest {
    private final UserMapper mapper = new UserMapper();

    @Mock
    private PasswordEncoder passwordEncoder;

    private RegisterRequestDTO getRegisterRequest() {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.firstName = " john ";
        dto.lastName = " doe ";
        dto.email = " john.doe@example.com ";
        dto.password = " secret ";
        dto.bsn = " 123456789 ";
        dto.phoneNumber = " +31612345678 ";
        return dto;
    }

    @Test
    void toEntityMapsAndTrimsFields() {
        RegisterRequestDTO request = getRegisterRequest();

        // Mock the password encoder to return a hashed password
        when(passwordEncoder.encode("secret")).thenReturn("hashed");

        User user = mapper.toEntity(request, passwordEncoder);

        // Verify that the fields are trimmed and mapped correctly
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("john.doe@example.com", user.getEmail());
        assertEquals("hashed", user.getPassword());
        assertEquals("123456789", user.getBsn());
        assertEquals("+31612345678", user.getPhoneNumber());
        assertEquals(UserAccountStatus.PENDING, user.getIsApproved());
        assertEquals(UserRole.CUSTOMER, user.getRole());

        // Verify that the password was encoded
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(passwordEncoder).encode(captor.capture());
        assertEquals("secret", captor.getValue());
    }

    @Test
    void toProfileDTOMapsAllFields() {
        User user = User.builder()
                .id(5L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("pw")
                .bsn("123456789")
                .phoneNumber("+31612345678")
                .isApproved(UserAccountStatus.APPROVED)
                .role(UserRole.EMPLOYEE)
                .build();

        UserProfileDTO dto = mapper.toProfileDTO(user);

        // Verify that all fields are mapped correctly
        assertEquals(5L, dto.id());
        assertEquals("John", dto.firstName());
        assertEquals("Doe", dto.lastName());
        assertEquals("john.doe@example.com", dto.email());
        assertEquals("123456789", dto.bsn());
        assertEquals("+31612345678", dto.phoneNumber());
        assertEquals(UserAccountStatus.APPROVED, dto.isApproved());
        assertEquals(UserRole.EMPLOYEE, dto.role());
    }
}