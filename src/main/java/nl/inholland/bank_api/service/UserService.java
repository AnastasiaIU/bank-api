package nl.inholland.bank_api.service;

import nl.inholland.bank_api.constant.ErrorMessages;
import nl.inholland.bank_api.constant.FieldNames;
import nl.inholland.bank_api.mapper.UserMapper;
import nl.inholland.bank_api.model.dto.*;
import nl.inholland.bank_api.model.entities.User;
import nl.inholland.bank_api.model.enums.ApprovalStatus;
import nl.inholland.bank_api.repository.UserRepository;
import nl.inholland.bank_api.util.JwtUtil;
import nl.inholland.bank_api.util.StringUtils;
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
            throw new IllegalArgumentException(StringUtils.fieldError(FieldNames.EMAIL, ErrorMessages.EMAIL_EXISTS));
        }

        if (userRepository.existsByBsn(dto.bsn.trim())) {
            throw new IllegalArgumentException(StringUtils.fieldError(FieldNames.BSN, ErrorMessages.BSN_EXISTS));
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
                .orElseThrow(() -> new UsernameNotFoundException(ErrorMessages.USER_NOT_FOUND));
    }

    public UserProfileDTO getProfileById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toProfileDTO)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
    }

    public List<UserProfileDTO> getPendingUsers() {
        return userRepository.findByIsApproved(ApprovalStatus.PENDING)
                .stream()
                .map(userMapper::toProfileDTO)
                .toList();
    }

    public void updateApprovalStatus(Long userId, ApprovalStatus approvalStatus) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setIsApproved(approvalStatus);
        userRepository.save(user);
    }

    public void createAccountsForUser(Long userId, List<AccountWithUserDTO> accounts) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        accountService.saveAccounts(user, accounts);
    }
}
