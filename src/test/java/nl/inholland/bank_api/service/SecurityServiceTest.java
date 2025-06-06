package nl.inholland.bank_api.service;

import nl.inholland.bank_api.model.entities.Account;
import nl.inholland.bank_api.model.entities.AtmTransaction;
import nl.inholland.bank_api.model.entities.User;
import nl.inholland.bank_api.repository.AccountRepository;
import nl.inholland.bank_api.repository.AtmTransactionRepository;
import nl.inholland.bank_api.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@Import(SecurityService.class)
class SecurityServiceTest {
    @Autowired
    private SecurityService securityService;

    @MockitoBean
    private AccountRepository accountRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private AtmTransactionRepository transactionRepository;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private void authenticate(String email) {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                email, "pw", java.util.Collections.emptyList()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void isOwnerOfAccountReturnsTrueForOwner() {
        String email = "john.doe@example.com";
        String iban = "NL01INHO0123456789";
        User user = User.builder().id(1L).email(email).build();
        Account account = Account.builder().iban(iban).user(user).build();

        authenticate(email);

        // Mock the repository calls
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(accountRepository.findByIban(iban)).thenReturn(Optional.of(account));

        // Assert that the service method returns true
        assertTrue(securityService.isOwnerOfAccount(iban));
    }

    @Test
    void isOwnerOfAccountReturnsFalseWhenNotAuthenticated() {
        String iban = "NL01INHO0123456789";
        SecurityContextHolder.clearContext();

        // Assert that the service method returns false when not authenticated
        assertFalse(securityService.isOwnerOfAccount(iban));
        verifyNoInteractions(accountRepository, userRepository);
    }

    @Test
    void isOwnerOfAccountReturnsFalseForDifferentUser() {
        String email = "john.doe@example.com";
        String iban = "NL01INHO0123456789";
        User currentUser = User.builder().id(1L).email(email).build();
        User otherUser = User.builder().id(2L).build();
        Account account = Account.builder().iban(iban).user(otherUser).build();

        authenticate(email);

        // Mock the repository calls
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(currentUser));
        when(accountRepository.findByIban(iban)).thenReturn(Optional.of(account));

        // Assert that the service method returns false
        assertFalse(securityService.isOwnerOfAccount(iban));
    }

    @Test
    void isOwnerOfTransactionAccountReturnsTrueForOwner() {
        String email = "john.doe@example.com";
        User user = User.builder().id(1L).email(email).build();
        Account account = Account.builder().user(user).build();
        AtmTransaction transaction = AtmTransaction.builder().id(5L).account(account).build();

        authenticate(email);

        // Mock the repository calls
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(transactionRepository.findById(5L)).thenReturn(Optional.of(transaction));

        // Assert that the service method returns true
        assertTrue(securityService.isOwnerOfTransactionAccount(5L));
    }

    @Test
    void isOwnerOfTransactionAccountReturnsFalseForDifferentUser() {
        String email = "john.doe@example.com";
        User currentUser = User.builder().id(1L).email(email).build();
        User otherUser = User.builder().id(2L).build();
        Account account = Account.builder().user(otherUser).build();
        AtmTransaction transaction = AtmTransaction.builder().id(5L).account(account).build();

        authenticate(email);

        // Mock the repository calls
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(currentUser));
        when(transactionRepository.findById(5L)).thenReturn(Optional.of(transaction));

        // Assert that the service method returns false
        assertFalse(securityService.isOwnerOfTransactionAccount(5L));
    }
}