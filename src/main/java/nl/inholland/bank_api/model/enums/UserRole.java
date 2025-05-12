package nl.inholland.bank_api.model.enums;

import org.springframework.security.core.GrantedAuthority;

public enum UserRole implements GrantedAuthority {
    CUSTOMER,
    EMPLOYEE;

    public String getAuthority() {
        return "ROLE_" + name();
    }
}
