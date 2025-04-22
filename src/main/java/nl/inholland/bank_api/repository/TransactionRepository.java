package nl.inholland.bank_api.repository;

import nl.inholland.bank_api.model.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> { }