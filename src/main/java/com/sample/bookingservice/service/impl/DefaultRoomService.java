package com.sample.bookingservice.service.impl;

import com.sample.bookingservice.model.Room;
import com.sample.bookingservice.repository.ReservationRepository;
import com.sample.bookingservice.repository.RoomRepository;
import com.sample.bookingservice.service.RoomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.List;

@Service
@Validated
public class DefaultRoomService implements RoomService {
    private static final Logger log = LoggerFactory.getLogger(DefaultRoomService.class);

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;

    public DefaultRoomService(final ReservationRepository reservationRepository, final RoomRepository roomRepository) {
        this.reservationRepository = reservationRepository;
        this.roomRepository = roomRepository;
    }

    public List<Room> getAvailableRooms(final LocalDate from, final LocalDate to, final Pageable pageable) {
        final List<Room> rooms = roomRepository.getAvailableRooms(from, to, pageable);

        if (log.isDebugEnabled()) {
            log.debug("Available rooms between {} and {}, for pageable: {} has size:{}, rooms:{}", from, to, pageable, rooms.size(), rooms);
        }

        return rooms;
    }
}
