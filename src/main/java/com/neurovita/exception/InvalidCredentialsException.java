package com.neurovita.exception;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String mensagem) {
        super(mensagem);
    }
}