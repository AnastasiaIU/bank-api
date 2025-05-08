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
        User user = new User();
        user.setFirstName(StringUtils.capitalize(dto.firstName.trim()));
        user.setLastName(StringUtils.capitalize(dto.lastName.trim()));
        user.setEmail(dto.email.trim());
        user.setPassword(encoder.encode(dto.password.trim()));
        user.setBsn(dto.bsn.trim());
        user.setPhoneNumber(dto.phoneNumber.trim());
        user.setApproved(false);
        user.setRole(UserRole.CUSTOMER);
        return user;
    }

    public UserProfileDTO toProfileDTO(User user) {
        return new UserProfileDTO(
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
