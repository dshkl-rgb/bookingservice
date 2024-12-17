package com.sample.bookingservice.controller;

import com.sample.bookingservice.dto.MakeReservationDto;
import com.sample.bookingservice.dto.ReservationDto;
import com.sample.bookingservice.facade.ReservationFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/reservations")
@Tag(name = "Reservations", description = "Endpoints for managing reservations")
public class ReservationController {
    private static final Logger log = LoggerFactory.getLogger(ReservationController.class);

    private final ReservationFacade reservationFacade;

    public ReservationController(final ReservationFacade reservationFacade) {
        this.reservationFacade = reservationFacade;
    }

    @Operation(summary = "Create a reservation", description = "Create a reservation for a specific room and dates.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Reservation successfully created"),
            @ApiResponse(responseCode = "400", description = "Validation error occurred"),
            @ApiResponse(responseCode = "409", description = "Room is already booked for the provided dates")
    })
    @PostMapping
    public ResponseEntity<ReservationDto> makeReservation(@Valid @RequestBody final MakeReservationDto makeReservationDto) {
        final ReservationDto reservationDto = reservationFacade.makeReservation(makeReservationDto.getStartDate(),
                makeReservationDto.getEndDate(), makeReservationDto.getRoomId());

        if (log.isDebugEnabled()) {
            log.debug("Made Reservation: {} for MakeReservationDto: {}", reservationDto, makeReservationDto);
        }

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header(HttpHeaders.LOCATION, "/reservations/" + reservationDto.getReservationId())
                .body(reservationDto);
    }

    @Operation(summary = "Cancel a reservation", description = "Cancel an existing reservation by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Reservation successfully canceled"),
            @ApiResponse(responseCode = "404", description = "Reservation not found"),
            @ApiResponse(responseCode = "409", description = "Reservation cannot be canceled due to conflicts")
    })
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<ReservationDto> cancelReservation(@Min(1) @PathVariable final long id) {
        final ReservationDto cancelledReservation = reservationFacade.cancelReservation(id);

        if (log.isDebugEnabled()) {
            log.debug("Cancelled reservation: {}, for id: {}", cancelledReservation, id);
        }

        return ResponseEntity.noContent().build();
    }
}