package com.sample.bookingservice.mapper;

import com.sample.bookingservice.dto.RoomDto;
import com.sample.bookingservice.model.Room;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface RoomMapper {
    RoomMapper INSTANCE = Mappers.getMapper(RoomMapper.class);

    RoomDto roomToRoomDto(final Room room);
    Room roomDtoToRoom(final RoomDto roomDto);
}
