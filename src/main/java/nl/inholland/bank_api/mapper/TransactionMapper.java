package nl.inholland.bank_api.mapper;

import nl.inholland.bank_api.model.dto.CombinedTransactionDTO;
import nl.inholland.bank_api.model.dto.TransactionRequestDTO;
import nl.inholland.bank_api.model.dto.TransactionResponseDTO;
import nl.inholland.bank_api.model.entities.Account;
import nl.inholland.bank_api.model.entities.Transaction;
import nl.inholland.bank_api.model.enums.Status;
import org.springframework.stereotype.Component;

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

}
