package nl.inholland.bank_api.service;

import nl.inholland.bank_api.mapper.UserMapper;
import nl.inholland.bank_api.model.dto.*;
import nl.inholland.bank_api.model.entities.Account;
import nl.inholland.bank_api.model.entities.User;
import nl.inholland.bank_api.model.enums.ApprovalStatus;
import nl.inholland.bank_api.repository.AccountRepository;
import nl.inholland.bank_api.repository.UserRepository;
import nl.inholland.bank_api.util.JwtUtil;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final AccountService accountService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository, AccountService accountService, PasswordEncoder passwordEncoder, UserMapper userMapper, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.accountService = accountService;
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

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole(), user.getId());
        return new LoginResponseDTO(token);
    }

    public UserProfileDTO getProfileByEmail(String email) {
        return userRepository.findByEmail(email.trim())
                .map(userMapper::toProfileDTO)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email.trim())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
    }



    public List<UserProfileDTO> getPendingUsers() {
        return userRepository.findByIsApproved(ApprovalStatus.PENDING)
                .stream()
                .map(userMapper::toProfileDTO)
                .toList();
    }

    public void updateApprovalStatus(Long userId, ApprovalStatus approvalStatus, List<AccountDTO> accounts) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setIsApproved(approvalStatus);
        userRepository.save(user);

        if (approvalStatus == ApprovalStatus.APPROVED) {
            accountService.saveAccounts(user, accounts);
        }
    }
}
