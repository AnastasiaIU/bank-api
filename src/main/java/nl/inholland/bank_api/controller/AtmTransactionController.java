package nl.inholland.bank_api.controller;

import jakarta.validation.Valid;
import nl.inholland.bank_api.model.dto.AtmTransactionDTO;
import nl.inholland.bank_api.model.dto.AtmTransactionRequestDTO;
import nl.inholland.bank_api.service.AtmTransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("atm")
public class AtmTransactionController {
    private final AtmTransactionService atmTransactionService;

    public AtmTransactionController(AtmTransactionService atmTransactionService) {
        this.atmTransactionService = atmTransactionService;
    }

    @PostMapping("transactions")
    public ResponseEntity<AtmTransactionDTO> createTransaction(@Valid @RequestBody AtmTransactionRequestDTO dto) {
        AtmTransactionDTO createdTransaction = atmTransactionService.createTransaction(dto);
        return ResponseEntity.status(201).body(createdTransaction);
    }
}
