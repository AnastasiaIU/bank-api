package nl.inholland.bank_api.service;

import nl.inholland.bank_api.mapper.AtmTransactionMapper;
import nl.inholland.bank_api.mapper.TransactionMapper;
import nl.inholland.bank_api.model.dto.CombinedTransactionDTO;
import nl.inholland.bank_api.model.dto.TransactionFilterDTO;
import nl.inholland.bank_api.repository.AtmTransactionRepository;
import nl.inholland.bank_api.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CombinedTransactionService {
    private final AtmTransactionRepository atmTransactionRepository;
    private final TransactionRepository transactionRepository;
    private final AtmTransactionMapper atmTransactionMapper;
    private final TransactionMapper transactionMapper;

    public CombinedTransactionService(AtmTransactionRepository atmTransactionRepository, TransactionRepository transactionRepository, AtmTransactionMapper atmTransactionMapper, TransactionMapper transactionMapper){
        this.atmTransactionRepository = atmTransactionRepository;
        this.transactionRepository = transactionRepository;
        this.atmTransactionMapper = atmTransactionMapper;
        this.transactionMapper = transactionMapper;
    }

    public Page<CombinedTransactionDTO> getFilteredTransactions(Long accountId, TransactionFilterDTO filterDTO, Pageable pageable) {
        Stream<CombinedTransactionDTO> combinedStream = getCombinedTransactionStream(accountId);
        List<CombinedTransactionDTO> filteredList = applyFilters(combinedStream, filterDTO).collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filteredList.size());
        List<CombinedTransactionDTO> pageContent = start <= end ? filteredList.subList(start, end) : Collections.emptyList();

        return new PageImpl<>(pageContent, pageable, filteredList.size());
    }

    private Stream<CombinedTransactionDTO> getCombinedTransactionStream(Long accountId) {
        Stream<CombinedTransactionDTO> transferStream = transactionRepository
                .findBySourceAccount_IdOrTargetAccount_Id(accountId, accountId)
                .stream()
                .map(transactionMapper::toCombinedDTO);

        Stream<CombinedTransactionDTO> atmStream = atmTransactionRepository
                .findByAccountId(accountId)
                .stream()
                .map(atmTransactionMapper::toCombinedDTO);

        return Stream.concat(transferStream, atmStream);
    }

    private Stream<CombinedTransactionDTO> applyFilters(Stream<CombinedTransactionDTO> stream, TransactionFilterDTO filterDTO) {
        return stream.filter(dto -> matchesFilters(dto, filterDTO));
    }

    private boolean matchesFilters(CombinedTransactionDTO dto, TransactionFilterDTO filterDTO) {
        return matchesDate(dto.timestamp, filterDTO) &&
                matchesAmount(dto.amount, filterDTO) &&
                matchesIbans(dto.sourceIban, dto.targetIban, filterDTO) &&
                matchesDescription(dto.description, filterDTO);
    }

    private boolean matchesDate(LocalDateTime timestamp, TransactionFilterDTO filter) {
        if (filter.getStartDate() != null && !filter.getStartDate().isBlank()) {
            LocalDate start = LocalDate.parse(filter.getStartDate());
            if (timestamp.toLocalDate().isBefore(start)) return false;
        }
        if (filter.getEndDate() != null && !filter.getEndDate().isBlank()) {
            LocalDate end = LocalDate.parse(filter.getEndDate());
            if (timestamp.toLocalDate().isAfter(end)) return false;
        }
        return true;
    }

    private boolean matchesDescription(String description, TransactionFilterDTO filter) {
        return filter.getDescription() == null || filter.getDescription().isBlank()
                || (description != null && description.toLowerCase().contains(filter.getDescription().toLowerCase()));
    }

    private boolean matchesAmount(BigDecimal amount, TransactionFilterDTO filter) {
        if (filter.getAmount() != null && filter.getComparison() != null) {
            int cmp = amount.compareTo(filter.getAmount());
            return switch (filter.getComparison()) {
                case "lt" -> cmp < 0;
                case "gt" -> cmp > 0;
                case "eq" -> cmp == 0;
                default -> true;
            };
        }
        return true;
    }

    private boolean matchesIbans(String sourceIban, String targetIban, TransactionFilterDTO filter) {
        if (filter.getSourceIban() != null && !filter.getSourceIban().isBlank() && !filter.getSourceIban().equals(sourceIban))
            return false;
        if (filter.getTargetIban() != null && !filter.getTargetIban().isBlank() && !filter.getTargetIban().equals(targetIban))
            return false;
        return true;
    }
}
