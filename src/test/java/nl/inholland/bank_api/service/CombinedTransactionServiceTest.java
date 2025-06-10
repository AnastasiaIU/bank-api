package nl.inholland.bank_api.service;

import nl.inholland.bank_api.model.dto.CombinedTransactionFullHistoryDTO;
import nl.inholland.bank_api.model.enums.Status;
import nl.inholland.bank_api.repository.CombinedTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CombinedTransactionServiceTest {
    @Mock
    private CombinedTransactionRepository combinedTransactionRepository;

    @InjectMocks
    private CombinedTransactionService combinedTransactionService;

    @Test
    void shouldReturnPagedCombinedTransactionDTOs() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 2);
        List<Object[]> mockRawResults = List.of(
                new Object[]{"NL01BANK123", "NL02BANK456", 1L, BigDecimal.valueOf(100), LocalDateTime.now(), "TRANSFER", Status.SUCCEEDED.toString()},
                new Object[]{"NL03BANK789", "NL04BANK012", 2L, BigDecimal.valueOf(200), LocalDateTime.now(), "WITHDRAW", Status.FAILED.toString()}
        );

        when(combinedTransactionRepository.findAllCombined(2, 0)).thenReturn(mockRawResults);
        when(combinedTransactionRepository.countAllCombined()).thenReturn(5L);

        // Act
        Page<CombinedTransactionFullHistoryDTO> result = combinedTransactionService.getAllCombinedTransactions(pageable);

        // Assert
        assertEquals(2, result.getContent().size());
        assertEquals(5, result.getTotalElements());
        assertEquals("NL01BANK123", result.getContent().get(0).sourceIban);
        assertEquals("NL03BANK789", result.getContent().get(1).sourceIban);

        verify(combinedTransactionRepository).findAllCombined(2, 0);
        verify(combinedTransactionRepository).countAllCombined();
    }
}