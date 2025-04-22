package nl.inholland.bank_api.repository;

import nl.inholland.bank_api.model.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findBySourceAccount_IdOrTargetAccount_Id(Long sourceId, Long targetId);
}