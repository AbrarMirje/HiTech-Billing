package com.hitechbilling.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvoiceNotFoundException.class)
    public ResponseEntity<?> handleInvoiceNotFoundException(InvoiceNotFoundException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
