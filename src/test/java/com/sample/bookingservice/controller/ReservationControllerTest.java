package com.sample.bookingservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sample.bookingservice.dto.MakeReservationDto;
import com.sample.bookingservice.dto.ReservationDto;
import com.sample.bookingservice.facade.ReservationFacade;
import com.sample.bookingservice.model.ReservationStatus;
import com.sample.bookingservice.model.Room;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Date;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservationController.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReservationFacade reservationFacade;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void shouldReturnNoContentWhenReservationIsCancelled() throws Exception {
        final int reservationId = 1;
        when(reservationFacade.cancelReservation(reservationId)).thenReturn(null);
        this.mockMvc.perform(delete("/reservations/{id}", reservationId))
                .andExpect(status().isNoContent());
    }

    @Test
    public void shouldMakeReservationSuccessfully() throws Exception {
        final Room room = new Room();
        room.setRoomId(1);
        room.setRoomNumber("room");

        final MakeReservationDto dto = new MakeReservationDto();
        final LocalDate startDate = LocalDate.now();
        final LocalDate endDate = LocalDate.now().plusDays(5);
        dto.setStartDate(startDate);
        dto.setEndDate(endDate);
        dto.setRoomId(room.getRoomId());

        final ReservationDto reservationDto = new ReservationDto();
        reservationDto.setStartDate(startDate);
        reservationDto.setEndDate(endDate);
        reservationDto.setReservationId(1L);
        reservationDto.setStatus(ReservationStatus.CONFIRMED);
        reservationDto.setCreatedAt(new Date());
        reservationDto.setRoom(room);


        when(reservationFacade.makeReservation(startDate, endDate, room.getRoomId())).thenReturn(reservationDto);

        this.mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reservationId", is(reservationDto.getReservationId().intValue())))
                .andExpect(jsonPath("$.startDate", is(startDate.toString())))
                .andExpect(jsonPath("$.endDate", is(endDate.toString())))
                .andExpect(jsonPath("$.status", is(ReservationStatus.CONFIRMED.toString())))
                .andExpect(jsonPath("$.room.roomNumber", is(room.getRoomNumber())))
                .andExpect(jsonPath("$.room.roomId", is(room.getRoomId())));
    }

    @Test
    public void shouldThrowErrorWhenMakeReservationDtoIsNull() throws Exception {
        this.mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldThrowErrorWhenStartDateIsNull() throws Exception {
        MakeReservationDto dto = new MakeReservationDto();
        dto.setStartDate(null);
        dto.setEndDate(LocalDate.now().plusDays(5));
        dto.setRoomId(1);

        this.mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldThrowErrorWhenEndDateIsNull() throws Exception {
        MakeReservationDto dto = new MakeReservationDto();
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(null);
        dto.setRoomId(1);

        this.mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldThrowErrorWhenRoomIdIsNull() throws Exception {
        MakeReservationDto dto = new MakeReservationDto();
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now().plusDays(5));
        dto.setRoomId(null);

        this.mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldThrowErrorWhenMakeReservationStartDateIsInThePast() throws Exception {
        MakeReservationDto dto = new MakeReservationDto();
        dto.setStartDate(LocalDate.now().minusDays(2));
        dto.setEndDate(LocalDate.now().plusDays(5));
        dto.setRoomId(1);

        this.mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldThrowErrorWhenMakeReservationEndDateIsInThePast() throws Exception {
        MakeReservationDto dto = new MakeReservationDto();
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now().minusDays(5));
        dto.setRoomId(1);

        this.mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldThrowErrorWhenMakeReservationEndDateIsToday() throws Exception {
        MakeReservationDto dto = new MakeReservationDto();
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now());
        dto.setRoomId(1);

        this.mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldThrowErrorWhenMakeReservationRoomIdIsNegativeOrZero() throws Exception {
        MakeReservationDto dto = new MakeReservationDto();
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now().plusDays(1));
        dto.setRoomId(-1);

        this.mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        dto.setRoomId(0);

        this.mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldThrowErrorWhenReservationIdNegativeOrZero() throws Exception {
        this.mockMvc.perform(delete("/reservations/{id}", 0))
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(delete("/reservations/{id}", -1))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldThrowErrorWhenReservationIdIncorrectValue() throws Exception {
        this.mockMvc.perform(delete("/reservations/{id}", "invalid"))
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(delete("/reservations/{id}", 1.23))
                .andExpect(status().isBadRequest());
    }

}