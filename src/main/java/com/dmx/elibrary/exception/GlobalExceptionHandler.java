package com.dmx.elibrary.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Turns every failure into an RFC 9457 Problem Details response with a
 * machine-readable {@code code} property.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ProblemDetail handleBusiness(BusinessException ex) {
        return problem(ex.getErrorCode(), ex.getMessage());
    }

    /**
     * A borrow/return raced with another request and lost the optimistic-lock
     * check on the book. The client can retry.
     */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ProblemDetail handleOptimisticLock(ObjectOptimisticLockingFailureException ex) {
        return problem(ErrorCode.CONCURRENT_UPDATE, ErrorCode.CONCURRENT_UPDATE.getDefaultMessage());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .orElse(ErrorCode.VALIDATION_FAILED.getDefaultMessage());
        return handleExceptionInternal(ex, problem(ErrorCode.VALIDATION_FAILED, detail), headers, status, request);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return problem(ErrorCode.INTERNAL_ERROR, ErrorCode.INTERNAL_ERROR.getDefaultMessage());
    }

    /**
     * Framework-level failures (unreadable body, bad path variable, unknown
     * route, wrong method/media type) are handled by the base class, which
     * emits a ProblemDetail without the {@code code} property. These overrides
     * attach it so every error response honors the ErrorCode contract.
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return handleExceptionInternal(ex, problem(ErrorCode.VALIDATION_FAILED, "Malformed request body"),
                headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return handleExceptionInternal(ex, problem(ErrorCode.VALIDATION_FAILED, ex.getPropertyName() + ": invalid value"),
                headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleNoResourceFoundException(NoResourceFoundException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return handleExceptionInternal(ex, problem(ErrorCode.NOT_FOUND, ErrorCode.NOT_FOUND.getDefaultMessage()),
                headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        headers.setAllow(ex.getSupportedHttpMethods());
        return handleExceptionInternal(ex, problem(ErrorCode.METHOD_NOT_ALLOWED,
                ErrorCode.METHOD_NOT_ALLOWED.getDefaultMessage()), headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        headers.setAccept(ex.getSupportedMediaTypes());
        return handleExceptionInternal(ex, problem(ErrorCode.MEDIA_TYPE_NOT_SUPPORTED,
                ErrorCode.MEDIA_TYPE_NOT_SUPPORTED.getDefaultMessage()), headers, status, request);
    }

    private static ProblemDetail problem(ErrorCode errorCode, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(errorCode.getStatus(), detail);
        problem.setProperty("code", errorCode.name());
        return problem;
    }
}
