package com.sample.bookingservice.repository;

import com.sample.bookingservice.model.Room;
import com.sample.bookingservice.validator.ConsistentReservationDateParameters;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.List;

@Validated
public interface RoomRepository extends JpaRepository<Room, Integer> {

    // we find reservations that DON'T have date intersection
    @ConsistentReservationDateParameters
    @Query("SELECT room from Reservation r, Room room WHERE r.room.roomId = room.roomId " +
            "AND NOT (r.startDate < :to AND :from < r.endDate)" +
            "AND r.status != ReservationStatus.CONFIRMED")
    List<Room> getAvailableRooms(@NotNull @FutureOrPresent @Param("from") final LocalDate from,
                                 @NotNull @Future @Param("to") final LocalDate to,
                                 final Pageable pageable);

    // If at least 1 dates intersection (status confirmed) found, room is not available
    // mind that we allow reservation to start on the same date that other reservation ended
    //TODO: more fine grained control over reservation statuses in the future
    @ConsistentReservationDateParameters
    @Query("SELECT count(r) = 0 from Reservation r WHERE r.room.roomId = :roomId " +
            "AND (r.startDate < :to AND :from < r.endDate)" +
            "AND r.status = ReservationStatus.CONFIRMED")
    boolean isRoomAvailable(@NotNull @FutureOrPresent @Param("from") final LocalDate from,
                            @NotNull @Future @Param("to") final LocalDate to,
                            @NotNull @Param("roomId") final Integer roomId);
}
