package nl.inholland.bank_api.service;

import nl.inholland.bank_api.repository.UserRepository;
import nl.inholland.bank_api.model.entities.User;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import nl.inholland.bank_api.model.enums.UserAccountStatus;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // This method is part of the UserDetailsService interface and must be named loadUserByUsername.
    // However, in our application, we use the user's email as the "username" for authentication.
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        if (user.getIsApproved() == UserAccountStatus.CLOSED   // ★
                || user.getIsApproved() == UserAccountStatus.REJECTED) {
            throw new DisabledException("Unable to login, account is closed or rejected");
        }

        // Return a UserDetails object (a Spring Security User object)
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                List.of(user.getRole())
        );
    }
}
