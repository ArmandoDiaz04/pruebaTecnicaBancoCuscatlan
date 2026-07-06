package com.pruebatecnica.bancocuscatlan.exception;

public class InvalidReservationStateException extends BadRequestException {

    public InvalidReservationStateException(String message) {
        super(message);
    }
}
