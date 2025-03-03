package ua.yarynych.apiaccountmanagement.entity.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class BadTokenException extends RuntimeException {

    public BadTokenException(String msg) { super(msg); }
}
