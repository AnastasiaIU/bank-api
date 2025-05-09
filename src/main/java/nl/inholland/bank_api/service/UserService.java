package nl.inholland.bank_api.service;

import nl.inholland.bank_api.mapper.UserMapper;
import nl.inholland.bank_api.model.dto.RegisterRequestDTO;
import nl.inholland.bank_api.model.dto.UserProfileDTO;
import nl.inholland.bank_api.model.entities.User;
import nl.inholland.bank_api.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    public Long createUser(RegisterRequestDTO dto) {
        if (userRepository.existsByEmail(dto.email.trim())) {
            throw new IllegalArgumentException("email: Email already exists");
        }

        if (userRepository.existsByBsn(dto.bsn.trim())) {
            throw new IllegalArgumentException("bsn: BSN already exists");
        }

        User user = userMapper.toEntity(dto, passwordEncoder);

        return userRepository.save(user).getId();
    }

    public UserProfileDTO getProfileByEmail(String email) {
        User user = userRepository.findByEmail(email.trim());
        return userMapper.toProfileDTO(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email.trim());
    }
}
