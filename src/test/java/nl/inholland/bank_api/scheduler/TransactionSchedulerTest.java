package nl.inholland.bank_api.scheduler;

import nl.inholland.bank_api.model.entities.AtmTransaction;
import nl.inholland.bank_api.service.AtmTransactionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionSchedulerTest {
    @Mock
    private AtmTransactionService atmTransactionService;

    @InjectMocks
    private TransactionScheduler scheduler;

    @Test
    void processAllPendingTransactionsShouldCallProcessTransactionForEachPending() {
        AtmTransaction transaction1 = AtmTransaction.builder().id(1L).build();
        AtmTransaction transaction2 = AtmTransaction.builder().id(2L).build();
        List<AtmTransaction> pendingTransactions = List.of(transaction1, transaction2);

        // Mock the behaviour of atmTransactionService to return the pending transactions
        when(atmTransactionService.getPendingTransactions()).thenReturn(pendingTransactions);

        scheduler.processAllPendingTransactions();

        // Verify that the service methods were called correctly
        verify(atmTransactionService).getPendingTransactions();
        verify(atmTransactionService).processTransaction(transaction1);
        verify(atmTransactionService).processTransaction(transaction2);
    }
}