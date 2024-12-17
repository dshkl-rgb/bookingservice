package com.sample.bookingservice.service;

import com.sample.bookingservice.model.Room;
import com.sample.bookingservice.validator.ConsistentReservationDateParameters;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.List;

@Validated
public interface RoomService {
    @ConsistentReservationDateParameters
    List<Room> getAvailableRooms(@NotNull @FutureOrPresent final LocalDate from, @NotNull @Future final LocalDate to,
                                 final Pageable pageable);
}
