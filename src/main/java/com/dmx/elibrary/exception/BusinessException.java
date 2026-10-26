package com.dmx.elibrary.exception;

/**
 * Domain-level failure mapped to an HTTP response by {@link GlobalExceptionHandler}.
 */
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
