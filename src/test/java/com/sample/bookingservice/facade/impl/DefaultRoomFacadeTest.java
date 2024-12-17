package com.sample.bookingservice.facade.impl;


import com.sample.bookingservice.dto.RoomDto;
import com.sample.bookingservice.facade.RoomFacade;
import com.sample.bookingservice.model.Room;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@SpringBootTest
class DefaultRoomFacadeTest {

    private static final Integer ALLOWED_DAYS_AHEAD = 500;
    private static final Integer ALLOWED_RESERVATION_DURATION = 30;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:17-alpine"
    );

    @MockBean
    private RoomService roomService;

    @Autowired
    private RoomFacade defaultRoomFacade;

    private Room room1;
    private Room room2;
    private RoomDto roomDto1;
    private RoomDto roomDto2;

    @BeforeEach
    void setUp() {

        room1 = new Room();
        room1.setRoomId(1);
        room1.setRoomNumber("101");

        room2 = new Room();
        room2.setRoomId(2);
        room2.setRoomNumber("102");

        roomDto1 = new RoomDto();
        roomDto1.setRoomId(1);
        roomDto1.setRoomNumber("101");

        roomDto2 = new RoomDto();
        roomDto2.setRoomId(2);
        roomDto2.setRoomNumber("102");
    }

    @Test
    void shouldReturnMappedAvailableRooms() {
        LocalDate from = LocalDate.now().plusDays(1);
        LocalDate to = LocalDate.now().plusDays(5);

        when(roomService.getAvailableRooms(from, to, null)).thenReturn(List.of(room1, room2));
        List<RoomDto> availableRooms = defaultRoomFacade.getAvailableRooms(from, to, null);

        assertEquals(2, availableRooms.size());
        assertEquals(List.of(roomDto1, roomDto2), availableRooms);
    }

    @Test
    void shouldReturnEmptySetWhenNoRoomsAvailable() {
        LocalDate from = LocalDate.now().plusDays(1);
        LocalDate to = LocalDate.now().plusDays(5);

        when(roomService.getAvailableRooms(from, to, null)).thenReturn(List.of());
        List<RoomDto> availableRooms = defaultRoomFacade.getAvailableRooms(from, to, null);

        assertEquals(0, availableRooms.size());
    }

    @Test
    public void shouldThrowValidationErrorWhenDurationIsMoreThanMax() {
        final LocalDate now = LocalDate.now();
        assertThrows(ValidationException.class, () -> {
            defaultRoomFacade.getAvailableRooms(now.plusDays(1), now.plusDays(ALLOWED_RESERVATION_DURATION + 2), null);
        });
    }

    @Test
    public void shouldThrowValidationErrorWhenStartDateIsNull() {
        assertThrows(ValidationException.class, () -> {
            defaultRoomFacade.getAvailableRooms(null, LocalDate.now().plusDays(1), null);
        });
    }

    @Test
    public void shouldThrowValidationErrorWhenStartDateIsTooFarInTheFuture() {
        final LocalDate now = LocalDate.now();
        assertThrows(ValidationException.class, () -> {
            defaultRoomFacade.getAvailableRooms(now.plusDays(ALLOWED_DAYS_AHEAD + 1),
                    now.plusDays(ALLOWED_DAYS_AHEAD + 2), null);
        });
    }

    @Test
    public void shouldThrowValidationErrorWhenEndDateIsTooFarInTheFuture() {
        final LocalDate now = LocalDate.now();
        assertThrows(ValidationException.class, () -> {
            defaultRoomFacade.getAvailableRooms(now, now.plusDays(ALLOWED_DAYS_AHEAD + 1), null);
        });
    }

    @Test
    public void shouldThrowValidationErrorWhenEndDateIsNull() {
        assertThrows(ValidationException.class, () -> {
            defaultRoomFacade.getAvailableRooms(LocalDate.now().plusDays(1), null, null);
        });
    }

    @Test
    public void shouldThrowValidationErrorWhenEndDateIsInThePast() {
        final LocalDate endDateInThePast = LocalDate.now().minusDays(1);
        assertThrows(ValidationException.class, () -> {
            defaultRoomFacade.getAvailableRooms(LocalDate.now().plusDays(1), endDateInThePast, null);
        });
    }

    @Test
    public void shouldThrowValidationErrorWhenStartDateIsInThePast() {
        final LocalDate startDateInThePast = LocalDate.now().minusDays(1);
        assertThrows(ValidationException.class, () -> {
            defaultRoomFacade.getAvailableRooms(startDateInThePast, LocalDate.now().plusDays(1), null);
        });
    }

    @Test
    public void shouldThrowValidationErrorWhenStartDateIsAfterEndDate() {
        final LocalDate startDate = LocalDate.now().plusDays(2);
        final LocalDate endDate = LocalDate.now().plusDays(1);
        assertThrows(ValidationException.class, () -> {
            defaultRoomFacade.getAvailableRooms(startDate, endDate, null);
        });
    }

    @Test
    public void shouldThrowValidationErrorWhenEndDateIsToday() {
        final LocalDate today = LocalDate.now();
        assertThrows(ValidationException.class, () -> {
            defaultRoomFacade.getAvailableRooms(LocalDate.now().plusDays(1), today, null);
        });
    }

    @Test
    public void shouldThrowValidationErrorWhenStartDateAndEndDateIsTheSame() {
        final LocalDate sameDate = LocalDate.now().plusDays(1);
        assertThrows(ValidationException.class, () -> {
            defaultRoomFacade.getAvailableRooms(sameDate, sameDate, null);
        });
    }

}