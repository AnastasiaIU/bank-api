package nl.inholland.bank_api.controller;

import jakarta.validation.Valid;
import nl.inholland.bank_api.model.dto.UserDTO;
import nl.inholland.bank_api.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("register")
    public ResponseEntity<?> register(@Valid @RequestBody UserDTO dto) {
        Long id = userService.register(dto);
        return ResponseEntity.status(201).body(Collections.singletonMap("id", id));
    }
}
