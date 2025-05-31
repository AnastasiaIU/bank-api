package nl.inholland.bank_api.scheduler;

import nl.inholland.bank_api.service.AtmTransactionService;
import nl.inholland.bank_api.service.TransactionService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TransactionScheduler {
    private final AtmTransactionService atmTransactionService;
    private final TransactionService transactionService;

    public TransactionScheduler(AtmTransactionService atmTransactionService, TransactionService transactionService) {
        this.atmTransactionService = atmTransactionService;
        this.transactionService = transactionService;
    }

    @Scheduled(fixedRate = 5000)
    public void processAllPendingTransactions() {
        atmTransactionService.processPendingTransactions();
        transactionService.processPendingTransactions();
    }
}
