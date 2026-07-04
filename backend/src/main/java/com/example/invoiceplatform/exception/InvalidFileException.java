package com.example.invoiceplatform.exception;

import org.springframework.http.HttpStatus;

public class InvalidFileException extends BusinessException {

    public InvalidFileException(String message) {
        super(HttpStatus.BAD_REQUEST, "INVALID_FILE", message);
    }
}
