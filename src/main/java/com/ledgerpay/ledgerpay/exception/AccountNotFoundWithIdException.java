package com.ledgerpay.ledgerpay.exception;

public class AccountNotFoundWithIdException extends RuntimeException {
    public AccountNotFoundWithIdException(String message) {
        super(message);
    }
}
