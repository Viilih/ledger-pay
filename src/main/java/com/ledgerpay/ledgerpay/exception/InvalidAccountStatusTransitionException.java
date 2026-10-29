package com.ledgerpay.ledgerpay.exception;

public class InvalidAccountStatusTransitionException extends RuntimeException {
    public InvalidAccountStatusTransitionException(String message) {
        super(message);
    }
}
