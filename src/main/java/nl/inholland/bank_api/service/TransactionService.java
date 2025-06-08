package nl.inholland.bank_api.service;

import nl.inholland.bank_api.mapper.TransactionMapper;
import nl.inholland.bank_api.model.dto.TransactionRequestDTO;
import nl.inholland.bank_api.model.dto.TransactionResponseDTO;
import nl.inholland.bank_api.model.entities.Transaction;
import nl.inholland.bank_api.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    public TransactionService(TransactionRepository transactionRepository, TransactionMapper transactionMapper) {
        this.transactionRepository = transactionRepository;
        this.transactionMapper = transactionMapper;
    }

    public Long postTransaction(TransactionRequestDTO dto) {
        Transaction transaction = transactionRepository.save(transactionMapper.toEntity(dto));
        return transaction.getId();
    }

    public List<TransactionResponseDTO> getAllTransactions() {
        return transactionRepository.findAll().stream().map(transactionMapper::toResponseDTO).toList();
    }
}