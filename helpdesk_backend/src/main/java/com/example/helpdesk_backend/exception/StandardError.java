package com.example.helpdesk_backend.exception;

import java.time.LocalDateTime;

public record StandardError(
    LocalDateTime timestamp,
    Integer status,
    String error,
    String path
) {

}
