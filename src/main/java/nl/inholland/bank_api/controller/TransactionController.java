package nl.inholland.bank_api.controller;

import jakarta.validation.Valid;
import nl.inholland.bank_api.model.dto.TransactionRequestDTO;
import nl.inholland.bank_api.model.dto.TransactionResponseDTO;
import nl.inholland.bank_api.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/transactions")
    public ResponseEntity<?> postTransaction(@Valid @RequestBody TransactionRequestDTO dto) {
        Long id = transactionService.postTransaction(dto);
        return ResponseEntity.status(201).body(Collections.singletonMap("id", id));
    }

    @GetMapping("/accounts/{accountId}/transactions")
    public ResponseEntity<List<TransactionResponseDTO>> getTransactionsByAccount(@PathVariable Long accountId, @RequestParam(required = false) String onDate,
                                                                                 @RequestParam(required = false) String before,
                                                                                 @RequestParam(required = false) String after,
                                                                                 @RequestParam(required = false) BigDecimal amount,
                                                                                 @RequestParam(required = false) String comparison,
                                                                                 @RequestParam(required = false) String sourceIban,
                                                                                 @RequestParam(required = false) String targetIban) {
        List<TransactionResponseDTO> transactions = transactionService.getFilteredTransactions(
                accountId, onDate, before, after, amount, comparison, sourceIban, targetIban
        );
        return ResponseEntity.ok(transactions);
    }
}