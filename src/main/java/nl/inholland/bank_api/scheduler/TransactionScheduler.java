package nl.inholland.bank_api.scheduler;

import nl.inholland.bank_api.service.AtmTransactionService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TransactionScheduler {
    private final AtmTransactionService atmTransactionService;

    public TransactionScheduler(AtmTransactionService atmTransactionService) {
        this.atmTransactionService = atmTransactionService;
    }

    @Scheduled(fixedRate = 5000)
    public void processAllPendingTransactions() {
        atmTransactionService.processPendingTransactions();
        // Add future: transactionService.processPendingTransactions();
    }
}
