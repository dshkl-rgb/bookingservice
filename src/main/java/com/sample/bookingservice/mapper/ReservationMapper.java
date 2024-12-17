package com.sample.bookingservice.mapper;

import com.sample.bookingservice.dto.ReservationDto;
import com.sample.bookingservice.model.Reservation;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ReservationMapper {
    ReservationMapper INSTANCE = Mappers.getMapper(ReservationMapper.class);
        
    ReservationDto toReservationDto(final Reservation reservation);
    Reservation toReservation(final ReservationDto reservationDto);
}
