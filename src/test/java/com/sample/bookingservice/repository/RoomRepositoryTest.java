package com.sample.bookingservice.repository;

import com.sample.bookingservice.model.Reservation;
import com.sample.bookingservice.model.ReservationStatus;
import com.sample.bookingservice.model.Room;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@TestPropertySource(properties = {"spring.jpa.hibernate.ddl-auto=create-drop"})
public class RoomRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:17-alpine"
    );


    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Value("#{new Integer('${reservation.days.ahead.allowed}')}")
    private Integer allowedDaysAhead;

    @Value("#{new Integer('${reservation.duration.allowed}')}")
    private Integer allowedResevationDuration;

    private Room room1;
    private Room room2;
    private Room room3;


    @BeforeEach
    public void setUp() {
        // Create and save rooms
        room1 = new Room();
        room1.setRoomNumber("101");
        roomRepository.save(room1);

        room2 = new Room();
        room2.setRoomNumber("102");
        roomRepository.save(room2);

        room3 = new Room();
        room3.setRoomNumber("103");
        roomRepository.save(room3);

        // Create reservations for room1
        Reservation reservation1 = new Reservation();
        reservation1.setRoom(room1);
        reservation1.setStatus(ReservationStatus.CONFIRMED);
        reservation1.setStartDate(LocalDate.now().plusDays(1));
        reservation1.setEndDate(LocalDate.now().plusDays(3));
        reservation1.setCreatedAt(new Date());
        reservationRepository.save(reservation1);

        // Create cancelled reservation for room2
        Reservation reservation2 = new Reservation();
        reservation2.setRoom(room2);
        reservation2.setStatus(ReservationStatus.CANCELLED);
        reservation2.setStartDate(LocalDate.now().plusDays(4));
        reservation2.setEndDate(LocalDate.now().plusDays(6));
        reservation2.setCreatedAt(new Date());
        reservationRepository.save(reservation2);

        // Create expired reservation for room3
        Reservation reservation3 = new Reservation();
        reservation3.setRoom(room3);
        reservation3.setStatus(ReservationStatus.EXPIRED);
        reservation3.setStartDate(LocalDate.now().minusDays(6));
        reservation3.setEndDate(LocalDate.now().minusDays(4));
        reservation3.setCreatedAt(new Date());
        reservationRepository.save(reservation3);

        // Save reservations to repository
        reservationRepository.save(reservation1);
        reservationRepository.save(reservation2);
        reservationRepository.save(reservation3);
    }

    @AfterEach
    public void tearDown() {
        reservationRepository.deleteAll();
        roomRepository.deleteAll();
    }


    @Test
    public void shouldReturnAvailableRoomsForSpecificDatesAndExcludeConfirmedReservations() {
        // Test date range
        LocalDate fromDate = LocalDate.now().plusDays(2);
        LocalDate toDate = LocalDate.now().plusDays(4);

        List<Room> availableRooms = roomRepository.getAvailableRooms(fromDate, toDate, null);

        // Assert that room1 is not available due to CONFIRMED reservation
        assertFalse(availableRooms.contains(room1));
        // Assert that room2 is available because its reservation is CANCELLED
        assertTrue(availableRooms.contains(room2));
        // Assert that room3 is available because its reservation is EXPIRED
        assertTrue(availableRooms.contains(room3));
    }

    @Test
    public void shouldReturnTrueWhenRoomIsAvailableForGivenDates() {
        // Test date range
        LocalDate fromDate = LocalDate.now().plusDays(7);
        LocalDate toDate = LocalDate.now().plusDays(10);

        // Check availability of room1
        boolean isRoom1Available = roomRepository.isRoomAvailable(fromDate, toDate, room1.getRoomId());
        assertTrue(isRoom1Available);

        // Check availability of room2
        boolean isRoom2Available = roomRepository.isRoomAvailable(fromDate, toDate, room2.getRoomId());
        assertTrue(isRoom2Available);
    }

    @Test
    public void shouldReturnTrueWhenReservationStartOnTheSameDateThatPreviousReservationEnds() {
        // Test date range
        LocalDate fromDate = LocalDate.now().plusDays(3);
        LocalDate toDate = LocalDate.now().plusDays(10);

        // Check availability of room1
        boolean isRoom1Available = roomRepository.isRoomAvailable(fromDate, toDate, room1.getRoomId());
        assertTrue(isRoom1Available);
    }

    @Test
    public void shouldReturnFalseWhenRoomIsUnavailableForGivenDates() {
        // Overlapping date range for room1's confirmed reservation
        LocalDate fromDate = LocalDate.now().plusDays(2);
        LocalDate toDate = LocalDate.now().plusDays(4);

        // Check availability of room1
        boolean isRoom1Available = roomRepository.isRoomAvailable(fromDate, toDate, room1.getRoomId());
        assertFalse(isRoom1Available);

        // Non-overlapping date range for room2's cancelled reservation
        LocalDate fromDate2 = LocalDate.now().plusDays(4);
        LocalDate toDate2 = LocalDate.now().plusDays(6);

        boolean isRoom2Available = roomRepository.isRoomAvailable(fromDate2, toDate2, room2.getRoomId());
        assertTrue(isRoom2Available);
    }

    @Test
    public void shouldThrowValidationErrorWhenDurationIsMoreThanMax() {
        final LocalDate now = LocalDate.now();
        assertThrows(ValidationException.class, () -> {
            roomRepository.getAvailableRooms(now.plusDays(1),
                    now.plusDays(allowedResevationDuration + 2), null);
        });
    }

    @Test
    public void shouldThrowValidationErrorWhenStartDateIsNull() {
        assertThrows(ValidationException.class, () -> {
            roomRepository.getAvailableRooms(null, LocalDate.now().plusDays(1), null);
        });
    }

    @Test
    public void shouldThrowValidationErrorWhenStartDateIsTooFarInTheFuture() {
        final LocalDate now = LocalDate.now();
        assertThrows(ValidationException.class, () -> {
            roomRepository.getAvailableRooms(now.plusDays(allowedDaysAhead + 1),
                    now.plusDays(allowedDaysAhead + 2), null);
        });
    }

    @Test
    public void shouldThrowValidationErrorWhenEndDateIsTooFarInTheFuture() {
        final LocalDate now = LocalDate.now();
        assertThrows(ValidationException.class, () -> {
            roomRepository.getAvailableRooms(now, now.plusDays(allowedDaysAhead + 1), null);
        });
    }

    @Test
    public void shouldThrowValidationErrorWhenEndDateIsNull() {
        assertThrows(ValidationException.class, () -> {
            roomRepository.getAvailableRooms(LocalDate.now().plusDays(1), null, null);
        });
    }

    @Test
    public void shouldThrowValidationErrorWhenEndDateIsInThePast() {
        final LocalDate endDateInThePast = LocalDate.now().minusDays(1);
        assertThrows(ValidationException.class, () -> {
            roomRepository.getAvailableRooms(LocalDate.now().plusDays(1), endDateInThePast, null);
        });
    }

    @Test
    public void shouldThrowValidationErrorWhenStartDateIsInThePast() {
        final LocalDate startDateInThePast = LocalDate.now().minusDays(1);
        assertThrows(ValidationException.class, () -> {
            roomRepository.getAvailableRooms(startDateInThePast, LocalDate.now().plusDays(1), null);
        });
    }

    @Test
    public void shouldThrowValidationErrorWhenStartDateIsAfterEndDate() {
        final LocalDate startDate = LocalDate.now().plusDays(2);
        final LocalDate endDate = LocalDate.now().plusDays(1);
        assertThrows(ValidationException.class, () -> {
            roomRepository.getAvailableRooms(startDate, endDate, null);
        });
    }

    @Test
    public void shouldThrowValidationErrorWhenEndDateIsToday() {
        final LocalDate today = LocalDate.now();
        assertThrows(ValidationException.class, () -> {
            roomRepository.getAvailableRooms(LocalDate.now().plusDays(1), today, null);
        });
    }

    @Test
    public void shouldThrowValidationErrorWhenStartDateAndEndDateIsTheSame() {
        final LocalDate sameDate = LocalDate.now().plusDays(1);
        assertThrows(ValidationException.class, () -> {
            roomRepository.getAvailableRooms(sameDate, sameDate, null);
        });
    }

}
