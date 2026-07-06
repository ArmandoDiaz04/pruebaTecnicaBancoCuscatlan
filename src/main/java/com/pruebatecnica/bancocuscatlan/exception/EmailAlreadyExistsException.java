package com.pruebatecnica.bancocuscatlan.exception;

public class EmailAlreadyExistsException extends BadRequestException {

    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
