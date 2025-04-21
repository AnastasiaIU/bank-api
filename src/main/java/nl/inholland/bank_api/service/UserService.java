package nl.inholland.bank_api.service;

import nl.inholland.bank_api.model.dto.UserDTO;
import nl.inholland.bank_api.model.entities.User;
import nl.inholland.bank_api.model.enums.UserRole;
import nl.inholland.bank_api.repository.UserRepository;
import nl.inholland.bank_api.util.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Long register(UserDTO dto) {
        if (userRepository.existsByEmail(dto.email.trim())) {
            throw new IllegalArgumentException("Email already exists");
        }

        if (userRepository.existsByBsn(dto.bsn.trim())) {
            throw new IllegalArgumentException("BSN already exists");
        }

        return userRepository.save(toUser(dto)).getId();
    }

    private User toUser(UserDTO dto) {
        User user = new User();
        user.setFirstName(StringUtils.capitalize(dto.firstName.trim()));
        user.setLastName(StringUtils.capitalize(dto.lastName.trim()));
        user.setEmail(dto.email.trim());
        user.setPassword(passwordEncoder.encode(dto.password.trim()));
        user.setBsn(dto.bsn.trim());
        user.setPhoneNumber(dto.phoneNumber.trim());
        user.setApproved(false);
        user.setRole(UserRole.CUSTOMER);
        return user;
    }

    public UserDTO toUserDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.id = user.getId();
        dto.firstName = user.getFirstName();
        dto.lastName = user.getLastName();
        dto.email = user.getEmail();
        dto.phoneNumber = user.getPhoneNumber();
        dto.bsn = user.getBsn();
        dto.isApproved = user.isApproved();
        dto.role = user.getRole().name();
        return dto;
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email.trim());
    }

}
