package nl.inholland.bank_api.mapper;

import nl.inholland.bank_api.model.dto.AccountWithUserDTO;
import nl.inholland.bank_api.model.entities.Account;
import nl.inholland.bank_api.model.entities.User;
import nl.inholland.bank_api.model.enums.AccountStatus;
import nl.inholland.bank_api.model.enums.AccountType;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {
    public AccountWithUserDTO toAccountWithUserDTO(Account account) {
        AccountWithUserDTO dto = new AccountWithUserDTO();
        dto.setId(account.getId());
        dto.setIban(account.getIban());
        dto.setStatus(account.getStatus().name());
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

    public Account toAccount(AccountWithUserDTO dto, User user) {
        Account account = new Account();
        account.setIban(dto.getIban());
        account.setStatus(AccountStatus.valueOf(dto.getStatus()));
        account.setType(AccountType.valueOf(dto.getType()));
        account.setBalance(dto.getBalance());
        account.setDailyLimit(dto.getDailyLimit());
        account.setWithdrawLimit(dto.getWithdrawLimit());
        account.setAbsoluteLimit(dto.getAbsoluteLimit());
        account.setUser(user);
        return account;
    }
}
