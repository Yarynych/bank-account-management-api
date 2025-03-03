package ua.yarynych.apiaccountmanagement.entity.auth;

import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ua.yarynych.apiaccountmanagement.entity.User;

import java.util.Collection;
import java.util.List;

@Log4j2
public class UserAuthDetails extends User implements UserDetails {
    private Collection<GrantedAuthority> authorities;

    public static UserAuthDetails build(String email, String role) {

        log.info("Building UserAuthDetails for email: {} with role: {}", email, role);

        UserAuthDetails details = new UserAuthDetails();
        details.setEmail(email);
        details.authorities = List.of(new SimpleGrantedAuthority(role));

        log.info("Assigned authorities: {}", details.authorities);
        return details;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        log.info("Returning authorities: {}", authorities);
        return authorities;
    }

    @Override
    public String getUsername() {
        return super.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String toString() {
        return String.format("%-12s %s", getEmail(), authorities);
    }
}
