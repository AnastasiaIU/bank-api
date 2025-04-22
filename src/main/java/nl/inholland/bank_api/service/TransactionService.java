package nl.inholland.bank_api.service;

import nl.inholland.bank_api.model.dto.TransactionDTO;
import nl.inholland.bank_api.model.entities.Account;
import nl.inholland.bank_api.model.entities.Transaction;
import nl.inholland.bank_api.repository.TransactionRepository;
import org.springframework.stereotype.Service;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountService accountService;

    public TransactionService(TransactionRepository transactionRepository, AccountService accountService) {
        this.transactionRepository = transactionRepository;
        this.accountService = accountService;
    }

    public Long postTransaction(TransactionDTO dto) {
        return transactionRepository.save(toTransaction(dto)).getId();
    }

    private Transaction toTransaction(TransactionDTO dto) {
        Transaction transaction = new Transaction();

        Account sourceAccount = accountService.fetchAccountById(dto.sourceAccount);
        Account targetAccount = accountService.fetchAccountById(dto.targetAccount);

        transaction.setSourceAccount(sourceAccount);
        transaction.setTargetAccount(targetAccount);
        transaction.setAmount(dto.amount);
        transaction.setDescription(dto.description);

        return transaction;
    }
}