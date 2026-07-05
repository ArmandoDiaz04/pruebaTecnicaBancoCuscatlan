package com.pruebaTecnica.BancoCuscatlan.exception;

public class OverlappingReservationException extends ConflictException {

    public OverlappingReservationException(String message) {
        super(message);
    }
}