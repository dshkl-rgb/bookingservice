package com.sample.bookingservice.dto;

import com.sample.bookingservice.model.ReservationStatus;
import com.sample.bookingservice.model.Room;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.Date;

@Validated
@Data
public class ReservationDto {
    @Min(1)
    @NotNull
    private Long reservationId;
    @NotNull
    private Room room;
    @NotNull
    private ReservationStatus status;
    @NotNull
    private LocalDate startDate;
    @NotNull
    private LocalDate endDate;
    @NotNull
    private Date createdAt = new Date();
}
