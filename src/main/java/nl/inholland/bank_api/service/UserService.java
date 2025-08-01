package nl.inholland.bank_api.service;

import nl.inholland.bank_api.constant.ErrorMessages;
import nl.inholland.bank_api.constant.FieldNames;
import nl.inholland.bank_api.exception.ConflictException;
import nl.inholland.bank_api.mapper.UserMapper;
import nl.inholland.bank_api.model.dto.*;
import nl.inholland.bank_api.model.entities.User;
import nl.inholland.bank_api.model.enums.UserAccountStatus;
import nl.inholland.bank_api.model.enums.UserRole;
import nl.inholland.bank_api.repository.UserRepository;
import nl.inholland.bank_api.util.JwtUtil;
import nl.inholland.bank_api.util.StringUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
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
            throw new ConflictException(StringUtils.fieldError(FieldNames.EMAIL, ErrorMessages.EMAIL_EXISTS));
        }

        if (userRepository.existsByBsn(dto.bsn.trim())) {
            throw new ConflictException(StringUtils.fieldError(FieldNames.BSN, ErrorMessages.BSN_EXISTS));
        }

        User user = userMapper.toEntity(dto, passwordEncoder);

        return userRepository.save(user).getId();
    }

    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        User user = userRepository.findByEmail(loginRequest.email.trim())
                .orElseThrow(() -> new BadCredentialsException(ErrorMessages.INVALID_EMAIL_OR_PASSWORD));

        if (!passwordEncoder.matches(loginRequest.password, user.getPassword())) {
            throw new BadCredentialsException(ErrorMessages.INVALID_EMAIL_OR_PASSWORD);
        }

        if (user.getIsApproved() == UserAccountStatus.CLOSED   // ★
                || user.getIsApproved() == UserAccountStatus.REJECTED) {
            throw new DisabledException("Unable to login, account is closed or rejected");
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

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
    }

    public List<UserProfileDTO> getPendingUsers() {
        return userRepository.findByIsApproved(UserAccountStatus.PENDING)
                .stream()
                .map(userMapper::toProfileDTO)
                .toList();
    }

    public void updateApprovalStatus(Long userId, UserAccountStatus userAccountStatus) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setIsApproved(userAccountStatus);
        userRepository.save(user);
    }

    public void createAccountsForUser(Long userId, List<AccountWithUserDTO> accounts) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        accountService.saveAccounts(user, accounts);
    }

    public List<UserProfileDTO> getActiveUsers() {
        return userRepository.findByIsApprovedAndRole(UserAccountStatus.APPROVED, UserRole.CUSTOMER)
                .stream()
                .map(userMapper::toProfileDTO)
                .toList();
    }

    public void closeUserAndAccounts(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setIsApproved(UserAccountStatus.CLOSED);
        userRepository.save(user);

        accountService.closeAllAccountsForUser(userId);
    }
}
