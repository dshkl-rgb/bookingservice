package com.sample.bookingservice.facade.impl;

import com.sample.bookingservice.dto.RoomDto;
import com.sample.bookingservice.facade.RoomFacade;
import com.sample.bookingservice.mapper.RoomMapper;
import com.sample.bookingservice.model.Room;
import com.sample.bookingservice.service.RoomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Validated
public class DefaultRoomFacade implements RoomFacade {
    private static final Logger log = LoggerFactory.getLogger(DefaultRoomFacade.class);

    private final RoomService roomService;

    public DefaultRoomFacade(final RoomService roomService) {
        this.roomService = roomService;
    }

    public List<RoomDto> getAvailableRooms(final LocalDate from, final LocalDate to, final Pageable pageable) {
        if (log.isDebugEnabled()) {
            log.debug("Get available rooms from {} to {} with pageable: {}", from, to, pageable);
        }

        final List<Room> availableRooms = roomService.getAvailableRooms(from, to, pageable);
        final List<RoomDto> availableRoomsDto = availableRooms
                .stream()
                .map(RoomMapper.INSTANCE::roomToRoomDto)
                .collect(Collectors.toList());

        if (log.isDebugEnabled()) {
            log.debug("Available rooms: {}, mapped to DTOs: {}", availableRooms, availableRoomsDto);
        }

        return availableRoomsDto;
    }
}
