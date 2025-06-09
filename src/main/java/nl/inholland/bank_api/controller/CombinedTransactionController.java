package nl.inholland.bank_api.controller;

import nl.inholland.bank_api.model.dto.CombinedTransactionFullHistoryDTO;
import nl.inholland.bank_api.service.CombinedTransactionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("combined-transactions")
public class CombinedTransactionController {
    private final CombinedTransactionService combinedTransactionService;

    public CombinedTransactionController(CombinedTransactionService combinedTransactionService) {
        this.combinedTransactionService = combinedTransactionService;
    }

    @GetMapping()
    public Page<CombinedTransactionFullHistoryDTO> getAllCombinedTransactions(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return combinedTransactionService.getAllCombinedTransactions(pageable);
    }
}