package com.sample.bookingservice.exception;

public class RoomAlreadyBookedException extends RuntimeException {
    public RoomAlreadyBookedException(String message) {
        super(message);
    }

    public RoomAlreadyBookedException() {
    }

    public RoomAlreadyBookedException(String message, Throwable cause) {
        super(message, cause);
    }

    public RoomAlreadyBookedException(Throwable cause) {
        super(cause);
    }
}