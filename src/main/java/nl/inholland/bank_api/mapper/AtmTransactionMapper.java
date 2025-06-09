package nl.inholland.bank_api.mapper;

import nl.inholland.bank_api.model.dto.AtmHistoryTransactionDTO;
import nl.inholland.bank_api.model.dto.AtmTransactionDTO;
import nl.inholland.bank_api.model.dto.AtmTransactionRequestDTO;
import nl.inholland.bank_api.model.dto.CombinedTransactionDTO;
import nl.inholland.bank_api.model.entities.Account;
import nl.inholland.bank_api.model.entities.AtmTransaction;
import nl.inholland.bank_api.model.entities.User;
import nl.inholland.bank_api.model.enums.AtmTransactionType;
import nl.inholland.bank_api.model.enums.Status;
import org.springframework.stereotype.Component;

@Component
public class AtmTransactionMapper {
    public AtmTransaction toEntity(AtmTransactionRequestDTO dto, Account account, User initiatedBy) {
        return AtmTransaction.builder()
                .account(account)
                .initiatedBy(initiatedBy)
                .type(dto.type)
                .amount(dto.amount)
                .status(Status.PENDING)
                .build();
    }

    public AtmTransactionDTO toAtmTransactionDTO(AtmTransaction entity) {
        return new AtmTransactionDTO(
                entity.getId(),
                entity.getAccount().getIban(),
                entity.getInitiatedBy().getId(),
                entity.getType(),
                entity.getAmount(),
                entity.getTimestamp(),
                entity.getStatus().name(),
                entity.getFailureReason()
        );
    }

    public CombinedTransactionDTO toCombinedDTO(AtmTransaction atm) {
        CombinedTransactionDTO dto = new CombinedTransactionDTO();
        dto.id = atm.getId();
        dto.type = "ATM";
        dto.sourceIban = atm.getType() == AtmTransactionType.WITHDRAW ? atm.getAccount().getIban() : null;
        dto.targetIban = atm.getType() == AtmTransactionType.DEPOSIT ? atm.getAccount().getIban() : null;
        dto.amount = atm.getAmount();
        dto.timestamp = atm.getTimestamp();
        dto.status = atm.getStatus();
        dto.failureReason = atm.getFailureReason();
        dto.description = atm.getType().name();
        return dto;
    }
}
