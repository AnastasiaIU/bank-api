package nl.inholland.bank_api.controller;

import nl.inholland.bank_api.model.dto.AccountWithUserDTO;
import nl.inholland.bank_api.model.dto.ApprovalStatusUpdateDTO;
import nl.inholland.bank_api.model.dto.UserProfileDTO;
import nl.inholland.bank_api.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users/pending")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<List<UserProfileDTO>> getPendingUsers() {
        List<UserProfileDTO> pendingUsers = userService.getPendingUsers();
        return ResponseEntity.ok(pendingUsers);
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<UserProfileDTO> getUser(@PathVariable Long id) {
        UserProfileDTO userProfileDTO = userService.getProfileById(id);
                return ResponseEntity.ok(userProfileDTO);
    }

    @GetMapping("/users/accounts/active")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<List<UserProfileDTO>> getActiveUsers() {
        List<UserProfileDTO> userProfileDTO = userService.getActiveUsers();
        return ResponseEntity.ok(userProfileDTO);
    }


    @PutMapping("/users/{id}/approval-status")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<Void> updateApprovalStatus(
            @PathVariable Long id,
            @RequestBody ApprovalStatusUpdateDTO request
    ) {
        userService.updateApprovalStatus(id, request.getUserAccountStatus());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/users/{id}/accounts")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<Void> createDefaultAccounts(
            @PathVariable Long id,
            @RequestBody List<AccountWithUserDTO> accounts
    ) {
        userService.createAccountsForUser(id, accounts);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/users/{id}/close")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<Void> closeUserWithAccounts(@PathVariable Long id) {
        userService.closeUserAndAccounts(id);
        return ResponseEntity.noContent().build();
    }
}
