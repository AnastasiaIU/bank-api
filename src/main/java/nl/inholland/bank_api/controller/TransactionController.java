package nl.inholland.bank_api.controller;

import jakarta.validation.Valid;
import nl.inholland.bank_api.model.dto.TransactionDTO;
import nl.inholland.bank_api.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@RestController
@RequestMapping("transactions")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("post-transaction")
    public ResponseEntity<?> postTransaction(@Valid @RequestBody TransactionDTO dto) {
        Long id = transactionService.postTransaction(dto);
        return ResponseEntity.status(201).body(Collections.singletonMap("id", id));
    }
}