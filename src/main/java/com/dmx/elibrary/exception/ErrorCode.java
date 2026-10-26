package com.dmx.elibrary.exception;

import org.springframework.http.HttpStatus;

/**
 * Machine-readable business error codes carried in the {@code code} property
 * of every RFC 9457 Problem Details response.
 */
public enum ErrorCode {

    USER_ID_MISSING(HttpStatus.BAD_REQUEST, "Missing or invalid X-User-Id header"),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "Request validation failed"),
    BOOK_NOT_FOUND(HttpStatus.NOT_FOUND, "Book not found"),
    PATRON_NOT_FOUND(HttpStatus.NOT_FOUND, "Patron not found"),
    LOAN_NOT_FOUND(HttpStatus.NOT_FOUND, "Loan not found"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found"),
    NO_COPIES_AVAILABLE(HttpStatus.CONFLICT, "No copies available for this book"),
    BOOK_ALREADY_BORROWED(HttpStatus.CONFLICT, "Book already borrowed by this patron"),
    LOAN_ALREADY_RETURNED(HttpStatus.CONFLICT, "Loan has already been returned"),
    CONCURRENT_UPDATE(HttpStatus.CONFLICT, "Concurrent update conflict, please retry"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "HTTP method not allowed"),
    MEDIA_TYPE_NOT_SUPPORTED(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Content type not supported"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");

    private final HttpStatus status;
    private final String defaultMessage;

    ErrorCode(HttpStatus status, String defaultMessage) {
        this.status = status;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
