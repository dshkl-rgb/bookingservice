package com.sample.bookingservice.service.impl;

import com.sample.bookingservice.exception.CancelReservationException;
import com.sample.bookingservice.exception.ReservationNotFoundException;
import com.sample.bookingservice.exception.RoomAlreadyBookedException;
import com.sample.bookingservice.model.Reservation;
import com.sample.bookingservice.model.ReservationStatus;
import com.sample.bookingservice.model.Room;
import com.sample.bookingservice.repository.ReservationRepository;
import com.sample.bookingservice.repository.RoomRepository;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class DefaultReservationServiceTest {

    private static final Integer ALLOWED_DAYS_AHEAD = 500;
    private static final Integer ALLOWED_RESERVATION_DURATION = 30;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:17-alpine"
    );

    @MockBean
    private RoomRepository roomRepository;

    @MockBean
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationService defaultReservationService;
    private Reservation reservation;
    private Room room;
    @Autowired
    private ReservationService reservationService;

    @BeforeEach
    public void setUp() {
        room = new Room();
        room.setRoomId(1);
        room.setRoomNumber("room1");
        reservation = new Reservation();
        reservation.setStartDate(LocalDate.now());
        reservation.setEndDate(LocalDate.now().plusDays(1));
        reservation.setRoom(room);
    }

    @Test
    public void shouldThrowErrorIfUnableToFindReservationToCancel() {
        final long id = 1;
        when(reservationRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(ReservationNotFoundException.class, () -> {
            defaultReservationService.cancelReservation(id);
        });
    }

    @Test
    public void shouldThrowErrorIfReservationToCancelIsCancelled() {
        final long id = 1;
        reservation.setStatus(ReservationStatus.CANCELLED);

        when(reservationRepository.findById(id)).thenReturn(Optional.of(reservation));
        assertThrows(CancelReservationException.class, () -> {
            defaultReservationService.cancelReservation(id);
        });
    }

    @Test
    public void shouldThrowErrorIfReservationToCancelIsExpired() {
        final long id = 1;
        reservation.setStatus(ReservationStatus.EXPIRED);

        when(reservationRepository.findById(id)).thenReturn(Optional.of(reservation));
        assertThrows(CancelReservationException.class, () -> {
            defaultReservationService.cancelReservation(id);
        });
    }

    @Test
    public void shouldThrowErrorIfReservationIdIsNegativeOrZero() {
        assertThrows(ValidationException.class, () -> {
            defaultReservationService.cancelReservation(-1);
        });

        assertThrows(ValidationException.class, () -> {
            defaultReservationService.cancelReservation(0);
        });
    }

    @Test
    public void shouldCancelReservation() {
        final long id = 1;
        reservation.setStatus(ReservationStatus.CONFIRMED);

        when(reservationRepository.findById(id)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(reservation)).thenReturn(reservation);
        final Reservation cancelledReservation = defaultReservationService.cancelReservation(id);

        assertEquals(ReservationStatus.CANCELLED, cancelledReservation.getStatus());
        assertEquals(reservation.getReservationId(), cancelledReservation.getReservationId());
    }

    @Test
    public void shouldThrowErrorIfRoomIsNotAvailable() {
        when(roomRepository.findById(reservation.getRoom().getRoomId())).thenReturn(Optional.of(room));
        when(roomRepository.isRoomAvailable(reservation.getStartDate(), reservation.getEndDate(), room.getRoomId()))
                .thenReturn(false);
        assertThrows(RoomAlreadyBookedException.class, () -> {
            defaultReservationService.makeReservation(reservation.getStartDate(), reservation.getEndDate(), room.getRoomId());
        });
    }

    @Test
    public void shouldMakeReservationIfRoomIsAvailable() {
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setRoom(room);
        when(roomRepository.findById(room.getRoomId())).thenReturn(Optional.of(room));
        when(roomRepository.isRoomAvailable(reservation.getStartDate(), reservation.getEndDate(), room.getRoomId()))
                .thenReturn(true);
        when(reservationRepository.save(any())).thenReturn(reservation);
        final Reservation newReservation = defaultReservationService.makeReservation(reservation.getStartDate(), reservation.getEndDate(), room.getRoomId());

        assertEquals(ReservationStatus.CONFIRMED, newReservation.getStatus());
        assertEquals(room.getRoomId(), newReservation.getRoom().getRoomId());
    }

    @Test
    public void shouldThrowValidationErrorWhenDurationIsMoreThanMax() {
        final LocalDate now = LocalDate.now();
        final LocalDate startDate = now.plusDays(1);
        final LocalDate endDate = now.plusDays(ALLOWED_RESERVATION_DURATION + 2);
        assertThrows(ValidationException.class, () -> {
            reservationService.makeReservation(startDate, endDate, room.getRoomId());
        });
    }

    @Test
    public void shouldThrowValidationErrorWhenStartDateIsNull() {
        final LocalDate now = LocalDate.now();
        final LocalDate startDate = null;
        final LocalDate endDate = now.plusDays(1);
        assertThrows(ValidationException.class, () -> {
            reservationService.makeReservation(startDate, endDate, room.getRoomId());
        });
    }

    @Test
    public void shouldThrowValidationErrorWhenStartDateIsTooFarInTheFuture() {
        final LocalDate now = LocalDate.now();
        final LocalDate startDate = now.plusDays(ALLOWED_DAYS_AHEAD + 1);
        final LocalDate endDate = now.plusDays(ALLOWED_DAYS_AHEAD + 2);
        assertThrows(ValidationException.class, () -> {
            reservationService.makeReservation(startDate, endDate, room.getRoomId());
        });
    }

    @Test
    public void shouldThrowValidationErrorWhenEndDateIsTooFarInTheFuture() {
        final LocalDate now = LocalDate.now();
        final LocalDate startDate = now.plusDays(ALLOWED_DAYS_AHEAD - 2);
        final LocalDate endDate = now.plusDays(ALLOWED_DAYS_AHEAD + 2);
        assertThrows(ValidationException.class, () -> {
            reservationService.makeReservation(startDate, endDate, room.getRoomId());
        });
    }

    @Test
    public void shouldThrowValidationErrorWhenEndDateIsNull() {
        final LocalDate now = LocalDate.now();
        final LocalDate startDate = now.plusDays(1);
        final LocalDate endDate = null;
        assertThrows(ValidationException.class, () -> {
            reservationService.makeReservation(startDate, endDate, room.getRoomId());
        });
    }

    @Test
    public void shouldThrowValidationErrorWhenEndDateIsInThePast() {
        final LocalDate now = LocalDate.now();
        final LocalDate startDate = now;
        final LocalDate endDate = now.minusDays(2);
        assertThrows(ValidationException.class, () -> {
            reservationService.makeReservation(startDate, endDate, room.getRoomId());
        });
    }

    @Test
    public void shouldThrowValidationErrorWhenStartDateIsInThePast() {
        final LocalDate now = LocalDate.now();
        final LocalDate startDate = now.minusDays(2);
        final LocalDate endDate = now.plusDays(2);
        assertThrows(ValidationException.class, () -> {
            reservationService.makeReservation(startDate, endDate, room.getRoomId());
        });
    }

    @Test
    public void shouldThrowValidationErrorWhenStartDateIsAfterEndDate() {
        final LocalDate startDate = LocalDate.now().plusDays(2);
        final LocalDate endDate = LocalDate.now().plusDays(1);
        assertThrows(ValidationException.class, () -> {
            reservationService.makeReservation(startDate, endDate, room.getRoomId());
        });
    }

    @Test
    public void shouldThrowValidationErrorWhenEndDateIsToday() {
        final LocalDate now = LocalDate.now();
        final LocalDate startDate = now;
        final LocalDate endDate = now;
        assertThrows(ValidationException.class, () -> {
            reservationService.makeReservation(startDate, endDate, room.getRoomId());
        });
    }

    @Test
    public void shouldThrowValidationErrorWhenStartDateAndEndDateIsTheSame() {
        final LocalDate now = LocalDate.now();
        final LocalDate startDate = now.plusDays(1);
        final LocalDate endDate = now.plusDays(1);
        assertThrows(ValidationException.class, () -> {
            reservationService.makeReservation(startDate, endDate, room.getRoomId());
        });
    }

}