package com.example.invoiceplatform.exception;

import org.springframework.http.HttpStatus;

public class InvoiceParsingException extends BusinessException {

    public InvoiceParsingException(String message, Throwable cause) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, "INVOICE_PARSE_ERROR", message);
        initCause(cause);
    }
}
