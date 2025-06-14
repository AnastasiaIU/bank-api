package nl.inholland.bank_api.service;

import nl.inholland.bank_api.mapper.AtmTransactionMapper;
import nl.inholland.bank_api.mapper.TransactionMapper;
import nl.inholland.bank_api.model.dto.CombinedTransactionDTO;
import nl.inholland.bank_api.model.dto.CombinedTransactionFullHistoryDTO;
import nl.inholland.bank_api.model.dto.TransactionFilterDTO;
import nl.inholland.bank_api.model.entities.Transaction;
import nl.inholland.bank_api.model.enums.Status;
import nl.inholland.bank_api.repository.AtmTransactionRepository;
import nl.inholland.bank_api.repository.CombinedTransactionRepository;
import nl.inholland.bank_api.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Import(CombinedTransactionService.class)
class CombinedTransactionServiceTest {
    @InjectMocks
    private CombinedTransactionService combinedTransactionService;

    @Mock
    private CombinedTransactionRepository combinedTransactionRepository;

    @Mock
    private AtmTransactionRepository atmTransactionRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AtmTransactionMapper atmTransactionMapper;

    @Mock
    private TransactionMapper transactionMapper;

    private final Long accountId = 1L;

    private CombinedTransactionDTO createDTO(BigDecimal amount, LocalDateTime time) {
        CombinedTransactionDTO dto = new CombinedTransactionDTO();
        dto.amount = amount;
        dto.timestamp = time;
        dto.status = Status.SUCCEEDED;
        return dto;
    }

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

    @Test
    void testGetFilteredTransactions_WithValidData_ReturnsPage() {
        Transaction transaction = new Transaction();
        transaction.setAmount(BigDecimal.valueOf(50));
        transaction.setTimestamp(LocalDateTime.now());

        CombinedTransactionDTO dto = createDTO(BigDecimal.valueOf(50), LocalDateTime.now());

        when(transactionRepository.findBySourceAccount_IdOrTargetAccount_Id(accountId, accountId))
                .thenReturn(List.of(transaction));
        when(transactionMapper.toCombinedDTO(transaction)).thenReturn(dto);
        when(atmTransactionRepository.findByAccountId(accountId))
                .thenReturn(List.of());

        Pageable pageable = PageRequest.of(0, 10);
        TransactionFilterDTO filterDTO = new TransactionFilterDTO(); // no filters

        Page<CombinedTransactionDTO> result = combinedTransactionService.getFilteredTransactions(accountId, filterDTO, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(BigDecimal.valueOf(50), result.getContent().get(0).amount);
    }

    @Test
    void testGetFilteredTransactions() {
        Long accountId = 1L;
        TransactionFilterDTO filterDTO = new TransactionFilterDTO();
        Pageable pageable = PageRequest.of(0, 10);

        Transaction transaction = new Transaction();
        transaction.setAmount(new BigDecimal("50"));
        transaction.setTimestamp(LocalDateTime.now());

        CombinedTransactionDTO dto = new CombinedTransactionDTO();
        dto.amount = new BigDecimal("50");
        dto.timestamp = LocalDateTime.now();
        dto.status = Status.SUCCEEDED;

        when(transactionRepository.findBySourceAccount_IdOrTargetAccount_Id(accountId, accountId))
                .thenReturn(List.of(transaction));
        when(transactionMapper.toCombinedDTO(transaction)).thenReturn(dto);
        when(atmTransactionRepository.findByAccountId(accountId)).thenReturn(List.of());

        Page<CombinedTransactionDTO> page = combinedTransactionService.getFilteredTransactions(accountId, filterDTO, pageable);

        assertEquals(1, page.getContent().size());
        assertEquals(dto.amount, page.getContent().get(0).amount);
    }

    @Test
    void testGetFilteredTransactions_ReturnsEmptyPageWhenNoData() {
        when(transactionRepository.findBySourceAccount_IdOrTargetAccount_Id(accountId, accountId))
                .thenReturn(List.of());
        when(atmTransactionRepository.findByAccountId(accountId))
                .thenReturn(List.of());

        TransactionFilterDTO filter = new TransactionFilterDTO();
        Pageable pageable = PageRequest.of(0, 10);

        Page<CombinedTransactionDTO> result = combinedTransactionService.getFilteredTransactions(accountId, filter, pageable);

        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void testGetFilteredTransactions_FilterByDateRange() {
        Transaction transaction = new Transaction();
        transaction.setAmount(new BigDecimal("50"));
        transaction.setTimestamp(LocalDateTime.of(2025, 6, 1, 12, 0));

        CombinedTransactionDTO dto = createDTO(new BigDecimal("50"), LocalDateTime.of(2025, 6, 1, 12, 0));

        when(transactionRepository.findBySourceAccount_IdOrTargetAccount_Id(accountId, accountId)).thenReturn(List.of(transaction));
        when(transactionMapper.toCombinedDTO(transaction)).thenReturn(dto);
        when(atmTransactionRepository.findByAccountId(accountId)).thenReturn(List.of());

        TransactionFilterDTO filterDTO = new TransactionFilterDTO();
        filterDTO.setStartDate("2025-05-31");
        filterDTO.setEndDate("2025-06-02");

        Pageable pageable = PageRequest.of(0, 10);
        Page<CombinedTransactionDTO> result = combinedTransactionService.getFilteredTransactions(accountId, filterDTO, pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testGetFilteredTransactions_FilterByAmount() {
        Transaction transaction = new Transaction();
        transaction.setAmount(new BigDecimal("100"));
        transaction.setTimestamp(LocalDateTime.now());

        CombinedTransactionDTO dto = createDTO(new BigDecimal("100"), LocalDateTime.now());

        when(transactionRepository.findBySourceAccount_IdOrTargetAccount_Id(accountId, accountId)).thenReturn(List.of(transaction));
        when(transactionMapper.toCombinedDTO(transaction)).thenReturn(dto);
        when(atmTransactionRepository.findByAccountId(accountId)).thenReturn(List.of());

        TransactionFilterDTO filterDTO = new TransactionFilterDTO();
        filterDTO.setAmount(new BigDecimal("50"));
        filterDTO.setComparison("gt");

        Pageable pageable = PageRequest.of(0, 10);
        Page<CombinedTransactionDTO> result = combinedTransactionService.getFilteredTransactions(accountId, filterDTO, pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testGetFilteredTransactions_FilterByDescription() {
        Transaction transaction = new Transaction();
        transaction.setAmount(new BigDecimal("200"));
        transaction.setTimestamp(LocalDateTime.now());

        CombinedTransactionDTO dto = createDTO(new BigDecimal("200"), LocalDateTime.now());
        dto.description = "Payment for Rent";

        when(transactionRepository.findBySourceAccount_IdOrTargetAccount_Id(accountId, accountId)).thenReturn(List.of(transaction));
        when(transactionMapper.toCombinedDTO(transaction)).thenReturn(dto);
        when(atmTransactionRepository.findByAccountId(accountId)).thenReturn(List.of());

        TransactionFilterDTO filterDTO = new TransactionFilterDTO();
        filterDTO.setDescription("rent");

        Pageable pageable = PageRequest.of(0, 10);
        Page<CombinedTransactionDTO> result = combinedTransactionService.getFilteredTransactions(accountId, filterDTO, pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testGetFilteredTransactions_FilterByIBAN() {
        Transaction transaction = new Transaction();
        transaction.setAmount(new BigDecimal("75"));
        transaction.setTimestamp(LocalDateTime.now());

        CombinedTransactionDTO dto = createDTO(new BigDecimal("75"), LocalDateTime.now());
        dto.sourceIban = "NL91ABNA0417164300";
        dto.targetIban = "NL22RABO0123456789";

        when(transactionRepository.findBySourceAccount_IdOrTargetAccount_Id(accountId, accountId)).thenReturn(List.of(transaction));
        when(transactionMapper.toCombinedDTO(transaction)).thenReturn(dto);
        when(atmTransactionRepository.findByAccountId(accountId)).thenReturn(List.of());

        TransactionFilterDTO filterDTO = new TransactionFilterDTO();
        filterDTO.setSourceIban("NL91ABNA0417164300");

        Pageable pageable = PageRequest.of(0, 10);
        Page<CombinedTransactionDTO> result = combinedTransactionService.getFilteredTransactions(accountId, filterDTO, pageable);

        assertEquals(1, result.getTotalElements());
    }
}