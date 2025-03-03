package ua.yarynych.apiaccountmanagement.entity.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ua.yarynych.apiaccountmanagement.entity.User;
import ua.yarynych.apiaccountmanagement.entity.auth.Token;
import ua.yarynych.apiaccountmanagement.entity.enums.Role;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
public class AuthResponse {
    private Long userId;
    private String email;
    private String phone;
    private String firstName;
    private String secondName;
    private Role role;
    private Date createdAt;
    private Token accessToken;
    private Token refreshToken;

    public AuthResponse(String firstName, String secondName) {
        this.firstName = firstName;
        this.secondName = secondName;
    }

    public static AuthResponse logout(User user) {
        return new AuthResponse(user.getFirstName(), user.getSecondName());
    }

    @Override
    public String toString() {
        return String.format("%s [%s %s] (%s) %d", email, firstName, secondName, role, userId);
    }
}
