package nl.inholland.bank_api.mapper;

import nl.inholland.bank_api.model.dto.*;
import nl.inholland.bank_api.model.entities.Transaction;
import org.springframework.stereotype.Component;
import nl.inholland.bank_api.model.entities.Account;
import nl.inholland.bank_api.model.enums.Status;

@Component
public class TransactionMapper {

    public CombinedTransactionDTO toCombinedDTO(Transaction t) {
        CombinedTransactionDTO dto = new CombinedTransactionDTO();
        dto.id = t.getId();
        dto.type = "TRANSFER";
        dto.sourceIban = t.getSourceAccount().getIban();
        dto.targetIban = t.getTargetAccount().getIban();
        dto.amount = t.getAmount();
        dto.description = t.getDescription();
        dto.timestamp = t.getTimestamp();
        dto.status = t.getStatus();
        return dto;
    }

    public TransactionResponseDTO toTransactionDTO(Transaction entity) {
        return new TransactionResponseDTO(
                entity.getId(),
                entity.getSourceAccount().getId(),
                entity.getTargetAccount().getId(),
                entity.getAmount(),
                entity.getDescription(),
                entity.getStatus().name(),
                entity.getSourceAccount().getIban(),
                entity.getTargetAccount().getIban(),
                entity.getTimestamp(),
                entity.getFailureReason()
        );
    }

    public Transaction toTransactionEntity(TransactionRequestDTO dto, Account source, Account target) {
        return Transaction.builder()
                .sourceAccount(source)
                .targetAccount(target)
                .amount(dto.amount)
                .status(Status.PENDING)
                .description(dto.description)
                .build();
    }
}
