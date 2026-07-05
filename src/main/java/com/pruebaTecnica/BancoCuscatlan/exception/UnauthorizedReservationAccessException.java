package com.pruebaTecnica.BancoCuscatlan.exception;

public class UnauthorizedReservationAccessException extends ForbiddenException {

    public UnauthorizedReservationAccessException(String message) {
        super(message);
    }
}
