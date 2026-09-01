package com.example.helpdesk_backend.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * Captura os erros das nossas regras de negócio (BusinessException)
     */

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<StandardError> handleBusinessException(BusinessException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        StandardError err = new StandardError(LocalDateTime.now(), status.value(), e.getMessage(),
                request.getRequestURI());

        return ResponseEntity.status(status).body(err);
    }

    /**
     * Captura os erros de validação das anotações @Valid (ex: @Email, @NotBlank)
     */

    // 1. Captura erros de validação dos DTOs (@Valid, @NotNull, @NotBlank)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StandardError> handleValidation(MethodArgumentNotValidException e,
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        String mensagem = e.getBindingResult().getFieldErrors().get(0).getDefaultMessage();

        StandardError err = new StandardError(LocalDateTime.now(), status.value(), mensagem, request.getRequestURI());

        return ResponseEntity.status(status).body(err);
    }

    // 2. Captura violações de regras do Banco de Dados (ex: campos NOT NULL
    // ausentes)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<StandardError> handleDataIntegrity(DataIntegrityViolationException e,
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        StandardError err = new StandardError(
                LocalDateTime.now(),
                status.value(),
                "Erro de integridade de dados. Verifique se todos os campos obrigatórios foram enviados.",
                request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    /**
     * Rota inexistente. Sem este handler o NoResourceFoundException caia no catch-all
     * de Exception e virava 500 -- o cliente nao conseguia distinguir "essa rota nao
     * existe" de "o servidor quebrou".
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<StandardError> handleNotFound(NoResourceFoundException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        StandardError err = new StandardError(
                LocalDateTime.now(), status.value(),
                "Recurso não encontrado.", request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    /**
     * Negativa vinda do Spring Security (@PreAuthorize e afins). Tambem caia no catch-all
     * e virava 500 -- armadilha com @EnableMethodSecurity ligado.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<StandardError> handleAccessDenied(AccessDeniedException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        StandardError err = new StandardError(
                LocalDateTime.now(), status.value(),
                "Seu perfil não tem permissão para acessar este recurso.", request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardError> handleGenericException(Exception e, HttpServletRequest request) {
        // O catch-all descartava a causa raiz sem registro: um 500 em producao nao
        // deixava rastro para diagnostico.
        log.error("Erro nao tratado em {}: {}", request.getRequestURI(), e.getMessage(), e);

        StandardError error = new StandardError(
                LocalDateTime.now(),

                HttpStatus.INTERNAL_SERVER_ERROR.value(),

                "Erro interno no servidor. Contate o Administrador.",

                request.getRequestURI());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler({ BadCredentialsException.class, UsernameNotFoundException.class })
    public ResponseEntity<StandardError> handleAuthenticationException(Exception e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.UNAUTHORIZED; // 401
        StandardError err = new StandardError(
                LocalDateTime.now(),
                status.value(),
                "E-mail ou senha inválidos.",
                request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }
}
