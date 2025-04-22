package nl.inholland.bank_api.model.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "transaction")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Account sourceAccount;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Account targetAccount;

    @Column(nullable = false)
    private BigDecimal amount;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime timestamp;

    @Column()
    private String description;
}