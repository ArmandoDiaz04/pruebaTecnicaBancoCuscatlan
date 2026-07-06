package com.pruebaTecnica.BancoCuscatlan.exception;

public class InvalidReservationStateException extends BadRequestException {

    public InvalidReservationStateException(String message) {
        super(message);
    }
}
