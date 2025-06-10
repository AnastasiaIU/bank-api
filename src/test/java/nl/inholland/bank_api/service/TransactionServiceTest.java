package nl.inholland.bank_api.service;

import nl.inholland.bank_api.mapper.TransactionMapper;
import nl.inholland.bank_api.model.dto.TransactionRequestDTO;
import nl.inholland.bank_api.model.entities.Transaction;
import nl.inholland.bank_api.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionServiceTest {

    private TransactionService transactionService;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        transactionService = new TransactionService(transactionRepository, transactionMapper);
    }

    @Test
    void postTransaction_ShouldSaveAndReturnTransactionId() {
        // Arrange
        TransactionRequestDTO dto = new TransactionRequestDTO();
        dto.sourceAccount = "NL01BANK1234567890";
        dto.targetAccount = "NL02BANK9876543210";
        dto.initiatedBy = 1L;
        dto.amount = new BigDecimal("250.00");
        dto.description = "Test Transaction";

        Transaction mockTransaction = new Transaction();
        mockTransaction.setId(42L);

        when(transactionMapper.toEntity(dto)).thenReturn(mockTransaction);
        when(transactionRepository.save(mockTransaction)).thenReturn(mockTransaction);

        // Act
        Long result = transactionService.postTransaction(dto);

        // Assert
        assertEquals(42L, result);
        verify(transactionMapper).toEntity(dto);
        verify(transactionRepository).save(mockTransaction);
    }
}
