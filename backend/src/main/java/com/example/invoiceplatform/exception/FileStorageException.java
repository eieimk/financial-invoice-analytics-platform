package com.example.invoiceplatform.exception;

import org.springframework.http.HttpStatus;

public class FileStorageException extends BusinessException {

    public FileStorageException(String message, Throwable cause) {
        super(HttpStatus.SERVICE_UNAVAILABLE, "STORAGE_ERROR", message);
        initCause(cause);
    }
}
