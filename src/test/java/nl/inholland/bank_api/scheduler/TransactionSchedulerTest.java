package nl.inholland.bank_api.scheduler;

import nl.inholland.bank_api.service.AtmTransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransactionSchedulerTest {
    @Mock
    private AtmTransactionService service;

    private TransactionScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new TransactionScheduler(service);
    }

    @Test
    void processAllPendingTransactionsDelegatesToService() {
        scheduler.processAllPendingTransactions();
        verify(service).processPendingTransactions();
    }
}