package ua.yarynych.apiaccountmanagement.entity.enums;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    ROLE_ADMIN,
    ROLE_INTERNAL_USER;

    @Override
    public String getAuthority() {
        return this.name();
    }
}
