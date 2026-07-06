package com.pruebatecnica.bancocuscatlan.exception;

public class OverlappingReservationException extends ConflictException {

    public OverlappingReservationException(String message) {
        super(message);
    }
}