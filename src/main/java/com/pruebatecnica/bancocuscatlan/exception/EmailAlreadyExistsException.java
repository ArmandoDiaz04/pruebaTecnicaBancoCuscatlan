package com.pruebaTecnica.BancoCuscatlan.exception;

public class EmailAlreadyExistsException extends BadRequestException {

    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
