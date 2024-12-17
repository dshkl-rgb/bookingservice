package com.sample.bookingservice.service.impl;

import com.sample.bookingservice.exception.CancelReservationException;
import com.sample.bookingservice.exception.ReservationNotFoundException;
import com.sample.bookingservice.exception.RoomAlreadyBookedException;
import com.sample.bookingservice.exception.RoomNotFoundException;
import com.sample.bookingservice.model.Reservation;
import com.sample.bookingservice.model.ReservationStatus;
import com.sample.bookingservice.model.Room;
import com.sample.bookingservice.repository.ReservationRepository;
import com.sample.bookingservice.repository.RoomRepository;
import com.sample.bookingservice.service.ReservationService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;

@Service
@Validated
public class DefaultReservationService implements ReservationService {
    private static final Logger log = LoggerFactory.getLogger(DefaultReservationService.class);

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;

    public DefaultReservationService(final ReservationRepository reservationRepository, RoomRepository roomRepository) {
        this.reservationRepository = reservationRepository;
        this.roomRepository = roomRepository;
    }

    @Transactional
    public Reservation makeReservation(final LocalDate startDate, final LocalDate endDate, final Integer roomId) {
        if (log.isDebugEnabled()) {
            log.debug("makeReservation with startDate: {}, endDate: {}, roomId: {}", startDate, endDate, roomId);
        }

        final Room room = roomRepository.findById(roomId).orElseThrow(() ->
                new RoomNotFoundException("Unable to find room with id: " + roomId));

        if (roomRepository.isRoomAvailable(startDate, endDate, roomId)) {
            final Reservation reservation = new Reservation();
            reservation.setRoom(room);
            reservation.setStartDate(startDate);
            reservation.setEndDate(endDate);
            reservation.setStatus(ReservationStatus.CONFIRMED);

            if (log.isDebugEnabled()) {
                log.debug("makeReservation new reservation: {}", reservation);
            }

            return reservationRepository.save(reservation);
        } else {
            throw new RoomAlreadyBookedException("The room is already booked for the provided dates");
        }
    }

    @Transactional
    public Reservation cancelReservation(final long id) {
        if (log.isDebugEnabled()) {
            log.debug("cancelReservation with id: {}", id);
        }

        final Reservation reservation = reservationRepository.findById(id).orElseThrow(() ->
                new ReservationNotFoundException("Unable to find reservation with id " + id));

        // TODO: more fine-grained status transition rules
        if (!reservation.getStatus().equals(ReservationStatus.CONFIRMED)) {
            throw new CancelReservationException("Unable to cancel reservation in status " + reservation.getStatus());
        }

        if (log.isDebugEnabled()) {
            log.debug("cancelReservation reservation: {}", reservation);
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        return reservationRepository.save(reservation);
    }
}
