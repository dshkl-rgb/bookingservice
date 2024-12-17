package com.sample.bookingservice.facade;

import com.sample.bookingservice.dto.RoomDto;
import com.sample.bookingservice.validator.ConsistentReservationDateParameters;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.List;

@Validated
public interface RoomFacade {
    @ConsistentReservationDateParameters
    List<RoomDto> getAvailableRooms(@NotNull @FutureOrPresent final LocalDate from, @NotNull @Future final LocalDate to,
                                    final Pageable pageable);
}
