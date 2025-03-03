package ua.yarynych.apiaccountmanagement.entity.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class Token {
    private String token;
    private final long expiresAt;

    public static Token from(String token, long date) {
        return new Token(token, date);
    }
}
