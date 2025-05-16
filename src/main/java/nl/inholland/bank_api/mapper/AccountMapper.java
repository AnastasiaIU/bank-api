package nl.inholland.bank_api.mapper;

import nl.inholland.bank_api.model.dto.AccountWithUserDTO;
import nl.inholland.bank_api.model.entities.Account;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {
    public AccountWithUserDTO toAccountWithUserDTO(Account account) {
        AccountWithUserDTO dto = new AccountWithUserDTO();
        dto.setIban(account.getIban());
        dto.setType(account.getType().name());
        dto.setBalance(account.getBalance());
        dto.setDailyLimit(account.getDailyLimit());
        dto.setAbsoluteLimit(account.getAbsoluteLimit());
        dto.setWithdrawLimit(account.getWithdrawLimit());

        if (account.getUser() != null) {
            dto.setFirstName(account.getUser().getFirstName());
            dto.setLastName(account.getUser().getLastName());
        }

        return dto;
    }
}
