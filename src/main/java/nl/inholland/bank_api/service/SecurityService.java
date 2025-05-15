package nl.inholland.bank_api.service;

import nl.inholland.bank_api.model.entities.Account;
import nl.inholland.bank_api.model.entities.User;
import nl.inholland.bank_api.repository.AccountRepository;
import nl.inholland.bank_api.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service("securityService")
public class SecurityService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public SecurityService(AccountRepository accountRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    public boolean isAccountOwner(String iban) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return false;

        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return false;

        Account account = accountRepository.findByIban(iban).orElse(null);
        if (account == null) return false;

        return user.getId().equals(account.getUser().getId());
    }
}
