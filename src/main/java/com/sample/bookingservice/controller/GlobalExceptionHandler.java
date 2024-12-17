package com.sample.bookingservice.controller;

import com.sample.bookingservice.exception.CancelReservationException;
import com.sample.bookingservice.exception.ReservationNotFoundException;
import com.sample.bookingservice.exception.RoomAlreadyBookedException;
import com.sample.bookingservice.exception.RoomNotFoundException;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Validation error occurred")
    })
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<String> handleValidationException(final ValidationException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "409", description = "Room is already booked for the provided dates")
    })
    @ExceptionHandler(RoomAlreadyBookedException.class)
    public ResponseEntity<String> handleMakeReservationException(final RoomAlreadyBookedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "409", description = "Unable to cancel reservation due to conflict")
    })
    @ExceptionHandler(CancelReservationException.class)
    public ResponseEntity<String> handleCancelReservationException(final CancelReservationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Reservation not found")
    })
    @ExceptionHandler(ReservationNotFoundException.class)
    public ResponseEntity<String> handleCancelReservationException(final ReservationNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Room not found")
    })
    @ExceptionHandler(RoomNotFoundException.class)
    public ResponseEntity<String> handleRoomNotFoundException(final RoomNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}

