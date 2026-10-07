package com.example.AISafePSOFT_26.authUsers.domain;

import com.example.AISafePSOFT_26.exceptions.DomainException;

public class InvalidCredentialsException extends DomainException {
    public InvalidCredentialsException() {
        super("Invalid credentials");
    }
}
