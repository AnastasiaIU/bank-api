package nl.inholland.bank_api.repository;

import nl.inholland.bank_api.model.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDate;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findBySourceAccount_IdOrTargetAccount_Id(Long sourceId, Long targetId);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.sourceAccount.id = :accountId " +
            "AND t.status = 'SUCCEEDED' " +
            "AND CAST(t.timestamp AS date) = :date")
    BigDecimal sumAmountForAccountToday(@Param("accountId") Long accountId,
            @Param("date") LocalDate date);

}