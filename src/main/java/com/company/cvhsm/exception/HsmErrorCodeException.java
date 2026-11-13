package com.company.cvhsm.exception;

import lombok.Getter;

/**
 * Exception thrown when the HSM returns a non-zero error code in a response.
 */
@Getter
public class HsmErrorCodeException extends HsmException {

    private final String errorCode;
    private final String responseCode;

    public HsmErrorCodeException(String errorCode, String responseCode) {
        super(String.format("HSM returned error code %s for response code %s", errorCode, responseCode));
        this.errorCode = errorCode;
        this.responseCode = responseCode;
    }
}
