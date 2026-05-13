package com.eaglebank.backend.exception;

/**
 * Thrown when a withdrawal cannot be processed due to insufficient account balance.
 */
public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(String message) {
        super(message);
    }
}
