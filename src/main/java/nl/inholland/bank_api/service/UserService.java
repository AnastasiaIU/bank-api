package nl.inholland.bank_api.service;

import nl.inholland.bank_api.mapper.UserMapper;
import nl.inholland.bank_api.model.dto.LoginRequestDTO;
import nl.inholland.bank_api.model.dto.LoginResponseDTO;
import nl.inholland.bank_api.model.dto.RegisterRequestDTO;
import nl.inholland.bank_api.model.dto.UserProfileDTO;
import nl.inholland.bank_api.model.entities.User;
import nl.inholland.bank_api.repository.UserRepository;
import nl.inholland.bank_api.util.JwtUtil;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
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

    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        User user = userRepository.findByEmail(loginRequest.email.trim())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(loginRequest.password, user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        return new LoginResponseDTO(token);
    }

    public UserProfileDTO getProfileByEmail(String email) {
        return userRepository.findByEmail(email.trim())
                .map(userMapper::toProfileDTO)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }
}
