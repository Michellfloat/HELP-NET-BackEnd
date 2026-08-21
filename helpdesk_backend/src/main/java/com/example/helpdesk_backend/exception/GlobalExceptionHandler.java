package com.example.helpdesk_backend.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * Captura os erros das nossas regras de negócio (BusinessException)
     */

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<StandardError>handleBusinessException(BusinessException e, HttpServletRequest request){
        StandardError error = new StandardError(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            e.getMessage(),
            request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Captura os erros de validação das anotações @Valid (ex: @Email, @NotBlank)
     */

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>>handleValidationExceptions(MethodArgumentNotValidException err){
        Map<String,String>error = new HashMap<>();

        //Itera sobre os erros capturados pelo String Validation
        err.getBindingResult().getAllErrors().forEach((errors) -> {
            String fieldName = ((FieldError) errors).getField();
            String errorMessage = errors.getDefaultMessage();
            error.put(fieldName, errorMessage);
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardError>handleGenericException(Exception e, HttpServletRequest request){
        StandardError error = new StandardError(
            LocalDateTime.now(),

            HttpStatus.INTERNAL_SERVER_ERROR.value(),

            "Erro interno no servidor. Contate o Administrador.",

            request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
public ResponseEntity<StandardError> handleAuthenticationException(Exception e, HttpServletRequest request) {
    HttpStatus status = HttpStatus.UNAUTHORIZED; // 401
    StandardError err = new StandardError(
            LocalDateTime.now(),
            status.value(),
            "E-mail ou senha inválidos.",
            request.getRequestURI()
    );
    return ResponseEntity.status(status).body(err);
    }
}
