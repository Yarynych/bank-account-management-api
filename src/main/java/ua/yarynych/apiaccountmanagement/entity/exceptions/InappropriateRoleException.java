package ua.yarynych.apiaccountmanagement.entity.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class InappropriateRoleException extends RuntimeException {
    public InappropriateRoleException(String msg) { super(msg); }
}
