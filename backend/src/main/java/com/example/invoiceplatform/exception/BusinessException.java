package com.example.invoiceplatform.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base exception for domain/business-rule violations. Carries the HTTP status
 * so the global handler can translate it without per-exception mapping tables.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;

    public BusinessException(HttpStatus status, String errorCode, String message) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }
}
