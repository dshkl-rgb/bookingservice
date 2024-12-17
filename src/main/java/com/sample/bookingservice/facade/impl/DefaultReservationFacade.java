package com.sample.bookingservice.facade.impl;

import com.sample.bookingservice.dto.ReservationDto;
import com.sample.bookingservice.facade.ReservationFacade;
import com.sample.bookingservice.mapper.ReservationMapper;
import com.sample.bookingservice.model.Reservation;
import com.sample.bookingservice.service.ReservationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;

@Component
@Validated
public class DefaultReservationFacade implements ReservationFacade {
    private static final Logger log = LoggerFactory.getLogger(DefaultReservationFacade.class);

    private final ReservationService reservationService;

    public DefaultReservationFacade(final ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    public ReservationDto makeReservation(final LocalDate startDate, final LocalDate endDate, final Integer roomId) {
        if (log.isDebugEnabled()) {
            log.debug("Making reservation with startDate: {}, endDate: {}, roomId: {}", startDate, endDate, roomId);
        }

        final Reservation reservation = reservationService.makeReservation(startDate, endDate, roomId);
        final ReservationDto reservationDto = ReservationMapper.INSTANCE.toReservationDto(reservation);

        if (log.isDebugEnabled()) {
            log.debug("Made Reservation: {}, mapped to reservation DTO: {}", reservation, reservationDto);
        }

        return reservationDto;
    }

    public ReservationDto cancelReservation(final long id) {
        if (log.isDebugEnabled()) {
            log.debug("Cancel reservation with id: {}", id);
        }
        final Reservation reservation = reservationService.cancelReservation(id);
        final ReservationDto reservationDto = ReservationMapper.INSTANCE.toReservationDto(reservation);

        if (log.isDebugEnabled()) {
            log.debug("Cancelled Reservation: {}, mapped to reservation DTO: {}", reservation, reservationDto);
        }

        return reservationDto;
    }
}
