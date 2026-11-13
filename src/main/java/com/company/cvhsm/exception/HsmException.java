package com.company.cvhsm.exception;

/**
 * Base exception for HSM-related errors.
 */
public class HsmException extends RuntimeException {

    public HsmException(String message) {
        super(message);
    }

    public HsmException(String message, Throwable cause) {
        super(message, cause);
    }
}
