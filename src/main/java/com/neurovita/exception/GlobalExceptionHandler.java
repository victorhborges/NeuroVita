package com.neurovita.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(ResourceNotFoundException.class) public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException exception) {

                Map<String, Object> resposta = Map.of(
                        "status", HttpStatus.NOT_FOUND.value(),
                        "mensagem", exception.getMessage()
                );

                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(resposta);
                }
        
        @ExceptionHandler(InvalidCredentialsException.class)public ResponseEntity<Map<String, Object>> handleInvalidCredentials(InvalidCredentialsException exception) {
                Map<String, Object> resposta = Map.of(
                "status", HttpStatus.UNAUTHORIZED.value(),
                "mensagem", exception.getMessage());

                return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(resposta);
        }
}