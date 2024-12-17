package com.sample.bookingservice.service;

import com.sample.bookingservice.model.Reservation;
import com.sample.bookingservice.validator.ConsistentReservationDateParameters;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;

@Validated
public interface ReservationService {
    @ConsistentReservationDateParameters
    Reservation makeReservation(@NotNull @FutureOrPresent final LocalDate startDate, @NotNull @Future final LocalDate endDate,
                                @NotNull @Min(1) final Integer roomId);

    Reservation cancelReservation(@Min(1) final long id);
}
