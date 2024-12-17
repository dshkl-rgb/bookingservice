package com.sample.bookingservice.exception;

public class CancelReservationException extends RuntimeException {
    public CancelReservationException(String message) {
        super(message);
    }

    public CancelReservationException() {
    }

    public CancelReservationException(String message, Throwable cause) {
        super(message, cause);
    }

    public CancelReservationException(Throwable cause) {
        super(cause);
    }
}
