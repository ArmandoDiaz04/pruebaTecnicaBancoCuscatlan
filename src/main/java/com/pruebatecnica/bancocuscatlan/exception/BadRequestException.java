package com.pruebaTecnica.BancoCuscatlan.exception;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
