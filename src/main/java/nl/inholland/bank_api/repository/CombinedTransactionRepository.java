package nl.inholland.bank_api.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CombinedTransactionRepository {

    @PersistenceContext
    private EntityManager entityManager;

    //used in unit testing
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<Object[]> findAllCombined(int size, int offset) {
        String sql = """
    SELECT sa.iban AS source_iban,
           ta.iban AS target_iban,
           t.initiated_by,
           t.amount,
           t.timestamp,
           CAST('TRANSFER' AS VARCHAR) AS type,
           t.status
    FROM transaction t
    LEFT JOIN account sa ON t.source_account_id = sa.id
    LEFT JOIN account ta ON t.target_account_id = ta.id

    UNION ALL

    SELECT a.iban,
           NULL,
           at.initiated_by,
           at.amount,
           at.timestamp,
            CAST(at.type AS VARCHAR),
           at.status
    FROM atm_transactions at
    LEFT JOIN account a ON at.account_id = a.id

    ORDER BY timestamp DESC
    LIMIT :size OFFSET :offset
    """;


        return entityManager.createNativeQuery(sql)
                .setParameter("size", size)
                .setParameter("offset", offset)
                .getResultList();
    }


    public long countAllCombined() {
        String sql = "SELECT COUNT(*) FROM (" +
                "SELECT t.id FROM transaction t " +
                "UNION ALL " +
                "SELECT a.id FROM atm_transactions a" +
                ") AS combined";

        Object result = entityManager.createNativeQuery(sql).getSingleResult();
        return ((Number) result).longValue();
    }
}