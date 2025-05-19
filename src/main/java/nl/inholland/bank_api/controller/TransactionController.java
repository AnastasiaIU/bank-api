package nl.inholland.bank_api.controller;

import jakarta.validation.Valid;
import nl.inholland.bank_api.model.dto.CombinedTransactionDTO;
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
    public ResponseEntity<List<CombinedTransactionDTO>> getAllAccountTransactions(
            @PathVariable Long accountId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) BigDecimal amount,
            @RequestParam(required = false) String comparison,
            @RequestParam(required = false) String sourceIban,
            @RequestParam(required = false) String targetIban
    ) {
        List<CombinedTransactionDTO> transactions = transactionService.getFilteredTransactions(
                accountId, startDate, endDate, amount, comparison, sourceIban, targetIban
        );
        return ResponseEntity.ok(transactions);
    }
}