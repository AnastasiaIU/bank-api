package nl.inholland.bank_api.repository;

import nl.inholland.bank_api.model.entities.AtmTransaction;
import nl.inholland.bank_api.model.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface AtmTransactionRepository extends JpaRepository<AtmTransaction, Long> {
    List<AtmTransaction> findByStatus(Status status);

    @Query("SELECT COALESCE(SUM(t.amount), 0) " +
            "FROM AtmTransaction t " +
            "WHERE t.account.id = :accountId " +
            "AND t.type = 'WITHDRAW' " +
            "AND CAST(t.timestamp AS date) = :date " +
            "AND t.status = 'SUCCEEDED'")
    BigDecimal sumTodayWithdrawalsByAccount(@Param("accountId") Long accountId,
                                            @Param("date") LocalDate date);
}
