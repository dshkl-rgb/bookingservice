package com.sample.bookingservice.service.impl;

import com.sample.bookingservice.model.Room;
import com.sample.bookingservice.repository.ReservationRepository;
import com.sample.bookingservice.repository.RoomRepository;
import com.sample.bookingservice.service.RoomService;
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
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest
class DefaultRoomServiceTest {

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
    private RoomService defaultRoomService;

    private Room room1;
    private Room room2;
    private Room room3;


    @BeforeEach
    public void setUp() {
        room1 = new Room();
        room1.setRoomId(1);
        room1.setRoomNumber("101");

        room2 = new Room();
        room2.setRoomId(2);
        room2.setRoomNumber("102");

        room3 = new Room();
        room3.setRoomId(3);
        room3.setRoomNumber("103");
    }

    @Test
    public void shouldReturnRoomsFromRepository() {
        final LocalDate startDate = LocalDate.now();
        final LocalDate endDate = LocalDate.now().plusDays(5);
        when(roomRepository.getAvailableRooms(startDate, endDate, null)).thenReturn(List.of(room1, room2, room3));

        final List<Room> roomsAvailable = defaultRoomService.getAvailableRooms(startDate, endDate, null);

        assertTrue(roomsAvailable.contains(room1));
        assertTrue(roomsAvailable.contains(room2));
        assertTrue(roomsAvailable.contains(room3));
    }

    @Test
    public void shouldReturnEmptySetWhenNoAvailableRooms() {
        final LocalDate startDate = LocalDate.now();
        final LocalDate endDate = LocalDate.now().plusDays(5);
        when(roomRepository.getAvailableRooms(startDate, endDate, null)).thenReturn(Collections.emptyList());

        final List<Room> roomsAvailable = defaultRoomService.getAvailableRooms(startDate, endDate, null);

        assertTrue(roomsAvailable.isEmpty());
    }


    @Test
    public void shouldThrowValidationErrorWhenDurationIsMoreThanMax() {
        final LocalDate now = LocalDate.now();
        assertThrows(ValidationException.class, () -> {
            defaultRoomService.getAvailableRooms(now.plusDays(1), now.plusDays(ALLOWED_RESERVATION_DURATION + 2), null);
        });
    }

    @Test
    public void shouldThrowValidationErrorWhenStartDateIsNull() {
        assertThrows(ValidationException.class, () -> {
            defaultRoomService.getAvailableRooms(null, LocalDate.now().plusDays(1), null);
        });
    }

    @Test
    public void shouldThrowValidationErrorWhenStartDateIsTooFarInTheFuture() {
        final LocalDate now = LocalDate.now();
        assertThrows(ValidationException.class, () -> {
            defaultRoomService.getAvailableRooms(now.plusDays(ALLOWED_DAYS_AHEAD + 1),
                    now.plusDays(ALLOWED_DAYS_AHEAD + 2), null);
        });
    }

    @Test
    public void shouldThrowValidationErrorWhenEndDateIsTooFarInTheFuture() {
        final LocalDate now = LocalDate.now();
        assertThrows(ValidationException.class, () -> {
            defaultRoomService.getAvailableRooms(now, now.plusDays(ALLOWED_DAYS_AHEAD + 1), null);
        });
    }

    @Test
    public void shouldThrowValidationErrorWhenEndDateIsNull() {
        assertThrows(ValidationException.class, () -> {
            defaultRoomService.getAvailableRooms(LocalDate.now().plusDays(1), null, null);
        });
    }

    @Test
    public void shouldThrowValidationErrorWhenEndDateIsInThePast() {
        final LocalDate endDateInThePast = LocalDate.now().minusDays(1);
        assertThrows(ValidationException.class, () -> {
            defaultRoomService.getAvailableRooms(LocalDate.now().plusDays(1), endDateInThePast, null);
        });
    }

    @Test
    public void shouldThrowValidationErrorWhenStartDateIsInThePast() {
        final LocalDate startDateInThePast = LocalDate.now().minusDays(1);
        assertThrows(ValidationException.class, () -> {
            defaultRoomService.getAvailableRooms(startDateInThePast, LocalDate.now().plusDays(1), null);
        });
    }

    @Test
    public void shouldThrowValidationErrorWhenStartDateIsAfterEndDate() {
        final LocalDate startDate = LocalDate.now().plusDays(2);
        final LocalDate endDate = LocalDate.now().plusDays(1);
        assertThrows(ValidationException.class, () -> {
            defaultRoomService.getAvailableRooms(startDate, endDate, null);
        });
    }

    @Test
    public void shouldThrowValidationErrorWhenEndDateIsToday() {
        final LocalDate today = LocalDate.now();
        assertThrows(ValidationException.class, () -> {
            defaultRoomService.getAvailableRooms(LocalDate.now().plusDays(1), today, null);
        });
    }

    @Test
    public void shouldThrowValidationErrorWhenStartDateAndEndDateIsTheSame() {
        final LocalDate sameDate = LocalDate.now().plusDays(1);
        assertThrows(ValidationException.class, () -> {
            defaultRoomService.getAvailableRooms(sameDate, sameDate, null);
        });
    }

}