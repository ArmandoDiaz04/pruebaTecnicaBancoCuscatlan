package com.pruebatecnica.bancocuscatlan.exception;

public class UnauthorizedReservationAccessException extends ForbiddenException {

    public UnauthorizedReservationAccessException(String message) {
        super(message);
    }
}
