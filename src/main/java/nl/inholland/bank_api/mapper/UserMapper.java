package nl.inholland.bank_api.mapper;

import nl.inholland.bank_api.model.dto.RegisterRequestDTO;
import nl.inholland.bank_api.model.dto.UserProfileDTO;
import nl.inholland.bank_api.model.entities.User;
import nl.inholland.bank_api.model.enums.UserRole;
import nl.inholland.bank_api.util.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public User toEntity(RegisterRequestDTO dto, PasswordEncoder encoder) {
        return User.builder()
                .firstName(StringUtils.capitalize(dto.firstName.trim()))
                .lastName(StringUtils.capitalize(dto.lastName.trim()))
                .email(dto.email.trim())
                .password(encoder.encode(dto.password.trim()))
                .bsn(dto.bsn.trim())
                .phoneNumber(dto.phoneNumber.trim())
                .isApproved(false)
                .role(UserRole.CUSTOMER)
                .build();
    }

    public UserProfileDTO toProfileDTO(User user) {
        return new UserProfileDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getBsn(),
                user.getPhoneNumber(),
                user.isApproved(),
                user.getRole()
        );
    }
}
