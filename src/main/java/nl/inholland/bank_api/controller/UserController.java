package nl.inholland.bank_api.controller;

import nl.inholland.bank_api.model.dto.ApprovalStatusUpdateDTO;
import nl.inholland.bank_api.model.dto.UserProfileDTO;
import nl.inholland.bank_api.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users/pending")
    public ResponseEntity<List<UserProfileDTO>> getPendingUsers() {
        List<UserProfileDTO> pendingUsers = userService.getPendingUsers();
        return ResponseEntity.ok(pendingUsers);
    }

    @PutMapping("/users/{id}/approval")
    public ResponseEntity<Void> updateApprovalStatus(
            @PathVariable Long id,
            @RequestBody ApprovalStatusUpdateDTO request
    ) {
        userService.updateApprovalStatus(id, request.getApprovalStatus());
        return ResponseEntity.noContent().build();
    }
}
