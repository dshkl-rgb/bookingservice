package com.sample.bookingservice.controller;

import com.sample.bookingservice.dto.RoomDto;
import com.sample.bookingservice.facade.RoomFacade;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoomController.class)
public class RoomControllerTest {

    private static final Integer ALLOWED_DAYS_AHEAD = 500;
    private static final Integer ALLOWED_RESERVATION_DURATION = 30;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoomFacade roomFacade;

    @Test
    void shouldReturnAvailableRooms() throws Exception {
        final LocalDate from = LocalDate.now();
        final LocalDate to = LocalDate.now().plusDays(2);
        final RoomDto room1 = new RoomDto();
        room1.setRoomId(1);
        room1.setRoomNumber("room1");

        final RoomDto room2 = new RoomDto();
        room2.setRoomId(2);
        room2.setRoomNumber("room2");
        final List<RoomDto> roomsAvailable = new ArrayList<>(List.of(room1, room2));
        final Pageable pageable = Pageable.ofSize(10);

        when(roomFacade.getAvailableRooms(from, to, pageable)).thenReturn(roomsAvailable);

        this.mockMvc.perform(get("/rooms")
                        .param("from", from.toString())
                        .param("page", Integer.toString(pageable.getPageNumber()))
                        .param("size", Integer.toString(pageable.getPageSize()))
                        .param("to", to.toString())
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.[0].roomId", is(room1.getRoomId())))
                .andExpect(jsonPath("$.content.[1].roomId", is(room2.getRoomId())))
                .andExpect(jsonPath("$.content.[0].roomNumber", is(room1.getRoomNumber())))
                .andExpect(jsonPath("$.content.[1].roomNumber", is(room2.getRoomNumber())));

    }

    @Test
    void shouldThrowValidationErrorInvalidDates() throws Exception {
        this.mockMvc.perform(get("/rooms")
                        .param("from", "invalid")
                        .param("to", "invalid"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldThrowValidationErrorIncorrectFormat() throws Exception {
        this.mockMvc.perform(get("/rooms")
                        .param("from", "02.12.2024")
                        .param("to", "12.12.2024"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldThrowErrorWithoutDates() throws Exception {
        this.mockMvc.perform(get("/rooms"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldThrowErrorWhenStartDateInThePast() throws Exception {
        final LocalDate now = LocalDate.now();
        this.mockMvc.perform(get("/rooms")
                        .param("from", now.minusDays(2).toString())
                        .param("to", now.plusDays(10).toString()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldThrowErrorWhenEndDateBeforeStartDate() throws Exception {
        final LocalDate now = LocalDate.now();
        this.mockMvc.perform(get("/rooms")
                        .param("from", now.plusDays(10).toString())
                        .param("to", now.plusDays(8).toString()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldThrowErrorWhenEndDateInThePast() throws Exception {
        final LocalDate now = LocalDate.now();
        this.mockMvc.perform(get("/rooms")
                        .param("from", now.plusDays(2).toString())
                        .param("to", now.minusDays(10).toString()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldThrowErrorWhenStartDateIsOutOfRange() throws Exception {
        final LocalDate now = LocalDate.now();
        this.mockMvc.perform(get("/rooms")
                        .param("from", now.plusDays(ALLOWED_DAYS_AHEAD + 10).toString())
                        .param("to", now.plusDays(ALLOWED_DAYS_AHEAD + 12).toString()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }


    @Test
    void shouldThrowErrorWhenEndDateIsOutOfRange() throws Exception {
        final LocalDate now = LocalDate.now();
        this.mockMvc.perform(get("/rooms")
                        .param("from", now.plusDays(ALLOWED_DAYS_AHEAD - 10).toString())
                        .param("to", now.plusDays(ALLOWED_DAYS_AHEAD + 12).toString()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldThrowErrorWhenDurationIsLongerThanMax() throws Exception {
        final LocalDate now = LocalDate.now();
        this.mockMvc.perform(get("/rooms")
                        .param("from", now.toString())
                        .param("to", now.plusDays(ALLOWED_RESERVATION_DURATION + 2).toString()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldThrowErrorWhenStartDateIsMissing() throws Exception {
        final LocalDate now = LocalDate.now();
        this.mockMvc.perform(get("/rooms")
                        .param("to", now.plusDays(ALLOWED_DAYS_AHEAD + 12).toString()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldThrowErrorWhenEndDateIsMissing() throws Exception {
        final LocalDate now = LocalDate.now();
        this.mockMvc.perform(get("/rooms")
                        .param("from", now.plusDays(ALLOWED_DAYS_AHEAD + 10).toString()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
