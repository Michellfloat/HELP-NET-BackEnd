package com.example.helpdesk_backend.exception;

import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }

}
