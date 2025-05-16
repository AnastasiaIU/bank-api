package nl.inholland.bank_api.service;

import nl.inholland.bank_api.model.entities.User;
import nl.inholland.bank_api.repository.AccountRepository;
import nl.inholland.bank_api.repository.AtmTransactionRepository;
import nl.inholland.bank_api.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service("securityService")
public class SecurityService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AtmTransactionRepository transactionRepository;

    public SecurityService(
            AccountRepository accountRepository,
            UserRepository userRepository,
            AtmTransactionRepository transactionRepository
    ) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        return userRepository.findByEmail(auth.getName()).orElse(null);
    }

    public boolean isOwnerOfAccount(String iban) {
        User currentUser = getCurrentUser();
        if (currentUser == null) return false;

        return accountRepository.findByIban(iban)
                .map(account -> currentUser.getId().equals(account.getUser().getId()))
                .orElse(false);
    }

    public boolean isOwnerOfTransactionAccount(Long id) {
        User currentUser = getCurrentUser();
        if (currentUser == null) return false;

        return transactionRepository.findById(id)
                .map(transaction -> transaction.getAccount().getUser().getId())
                .map(ownerId -> ownerId.equals(currentUser.getId()))
                .orElse(false);
    }
}
