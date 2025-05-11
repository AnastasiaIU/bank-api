package nl.inholland.bank_api.service;

import nl.inholland.bank_api.repository.UserRepository;
import nl.inholland.bank_api.model.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService{

    @Autowired
    private UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // This method is part of the UserDetailsService interface and must be named loadUserByUsername.
    // However, in our application, we use the user's email as the "username" for authentication.
    @Override
    public UserDetails loadUserByUsername(String email) {
        User user = userRepository.findByEmail(email);

        // Return a UserDetails object (a Spring Security User object)
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                List.of(user.getRole())
        );
    }
}
