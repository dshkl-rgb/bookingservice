package com.sample.bookingservice.facade.impl;

import com.sample.bookingservice.dto.MakeReservationDto;
import com.sample.bookingservice.dto.ReservationDto;
import com.sample.bookingservice.facade.ReservationFacade;
import com.sample.bookingservice.model.Reservation;
import com.sample.bookingservice.model.ReservationStatus;
import com.sample.bookingservice.model.Room;
import com.sample.bookingservice.service.ReservationService;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import java.time.LocalDate;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@SpringBootTest
class DefaultReservationFacadeTest {

    private static final Integer ALLOWED_DAYS_AHEAD = 500;
    private static final Integer ALLOWED_RESERVATION_DURATION = 30;
    private static final Integer ROOM_ID = 1;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:17-alpine"
    );

    @MockBean
    private ReservationService reservationService;

    @Autowired
    private ReservationFacade defaultReservationFacade;

    private MakeReservationDto makeReservationDto;
    private Reservation reservation;
    private Room room;
    private LocalDate now;

    @BeforeEach
    void setUp() {
        now = LocalDate.now();
        room = new Room();
        room.setRoomId(ROOM_ID);
        room.setRoomNumber("room");

        reservation = new Reservation();
        reservation.setReservationId(1L);
        reservation.setStartDate(now);
        reservation.setEndDate(now.plusDays(1));
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setCreatedAt(new Date());
    }

    @Test
    public void shouldReturnMappedReservationWhenReservationWasMade() {
        final LocalDate startDate = now.plusDays(1);
        final LocalDate endDate = now.plusDays(2);
        when(reservationService.makeReservation(startDate, endDate, ROOM_ID)).thenReturn(reservation);
        final ReservationDto reservationDto = defaultReservationFacade.makeReservation(startDate, endDate, ROOM_ID);

        assertEquals(reservation.getReservationId(), reservationDto.getReservationId());
        assertEquals(reservation.getStartDate(), reservationDto.getStartDate());
        assertEquals(reservation.getEndDate(), reservationDto.getEndDate());
        assertEquals(reservation.getStatus(), reservationDto.getStatus());
        assertEquals(reservation.getCreatedAt(), reservationDto.getCreatedAt());
    }

    @Test
    public void shouldReturnMappedCancelledReservationWhenReservationWasCancelled() {
        when(reservationService.cancelReservation(reservation.getReservationId())).thenReturn(reservation);
        final ReservationDto reservationDto = defaultReservationFacade.cancelReservation(reservation.getReservationId());

        assertEquals(reservation.getReservationId(), reservationDto.getReservationId());
        assertEquals(reservation.getStartDate(), reservationDto.getStartDate());
        assertEquals(reservation.getEndDate(), reservationDto.getEndDate());
        assertEquals(reservation.getStatus(), reservationDto.getStatus());
        assertEquals(reservation.getCreatedAt(), reservationDto.getCreatedAt());
    }

    @Test
    public void shouldThrowErrorIfReservationIdIsNegativeOrZero() {
        assertThrows(ValidationException.class, () -> {
            defaultReservationFacade.cancelReservation(-1);
        });

        assertThrows(ValidationException.class, () -> {
            defaultReservationFacade.cancelReservation(0);
        });
    }

    @Test
    public void shouldThrowValidationErrorWhenDurationIsMoreThanMax() {
        final LocalDate startDate = now.plusDays(1);
        final LocalDate endDate = now.plusDays(ALLOWED_RESERVATION_DURATION + 2);
        assertThrows(ValidationException.class, () -> {
            defaultReservationFacade.makeReservation(startDate, endDate, ROOM_ID);
        });
    }

    @Test
    public void shouldThrowValidationErrorWhenStartDateIsNull() {
        final LocalDate startDate = null;
        final LocalDate endDate = now.plusDays(ALLOWED_RESERVATION_DURATION + 2);
        assertThrows(ValidationException.class, () -> {
            defaultReservationFacade.makeReservation(startDate, endDate, ROOM_ID);
        });
    }

    @Test
    public void shouldThrowValidationErrorWhenStartDateIsTooFarInTheFuture() {
        final LocalDate startDate = now.plusDays(ALLOWED_DAYS_AHEAD + 1);
        final LocalDate endDate = now.plusDays(ALLOWED_DAYS_AHEAD + 2);
        assertThrows(ValidationException.class, () -> {
            defaultReservationFacade.makeReservation(startDate, endDate, ROOM_ID);
        });
    }

    @Test
    public void shouldThrowValidationErrorWhenEndDateIsTooFarInTheFuture() {
        final LocalDate startDate = now;
        final LocalDate endDate = now.plusDays(ALLOWED_DAYS_AHEAD + 1);
        assertThrows(ValidationException.class, () -> {
            defaultReservationFacade.makeReservation(startDate, endDate, ROOM_ID);
        });
    }

    @Test
    public void shouldThrowValidationErrorWhenEndDateIsNull() {
        final LocalDate startDate = now;
        final LocalDate endDate = null;
        assertThrows(ValidationException.class, () -> {
            defaultReservationFacade.makeReservation(startDate, endDate, ROOM_ID);
        });
    }

    @Test
    public void shouldThrowValidationErrorWhenEndDateIsInThePast() {
        final LocalDate startDate = now.plusDays(1);
        final LocalDate endDate = now.minusDays(2);
        assertThrows(ValidationException.class, () -> {
            defaultReservationFacade.makeReservation(startDate, endDate, ROOM_ID);
        });
    }

    @Test
    public void shouldThrowValidationErrorWhenStartDateIsInThePast() {
        final LocalDate startDate = now.minusDays(2);
        final LocalDate endDate = now.plusDays(2);
        assertThrows(ValidationException.class, () -> {
            defaultReservationFacade.makeReservation(startDate, endDate, ROOM_ID);
        });
    }

    @Test
    public void shouldThrowValidationErrorWhenStartDateIsAfterEndDate() {
        final LocalDate startDate = now.plusDays(2);
        final LocalDate endDate = now.plusDays(1);
        assertThrows(ValidationException.class, () -> {
            defaultReservationFacade.makeReservation(startDate, endDate, ROOM_ID);
        });
    }

    @Test
    public void shouldThrowValidationErrorWhenEndDateIsToday() {
        final LocalDate startDate = now.plusDays(1);
        final LocalDate endDate = now;
        assertThrows(ValidationException.class, () -> {
            defaultReservationFacade.makeReservation(startDate, endDate, ROOM_ID);
        });
    }

    @Test
    public void shouldThrowValidationErrorWhenStartDateAndEndDateIsTheSame() {
        final LocalDate startDate = now.plusDays(1);
        final LocalDate endDate = now.plusDays(1);
        assertThrows(ValidationException.class, () -> {
            defaultReservationFacade.makeReservation(startDate, endDate, ROOM_ID);
        });
    }
}