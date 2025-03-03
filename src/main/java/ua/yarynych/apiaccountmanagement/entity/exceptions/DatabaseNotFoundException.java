package ua.yarynych.apiaccountmanagement.entity.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class DatabaseNotFoundException extends RuntimeException {

    public DatabaseNotFoundException(String msg) {
        super(msg);
    }
}