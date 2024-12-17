package com.sample.bookingservice.controller;

import com.sample.bookingservice.dto.RoomDto;
import com.sample.bookingservice.facade.RoomFacade;
import com.sample.bookingservice.validator.ConsistentReservationDateParameters;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.util.MimeTypeUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RequestMapping(value = "/rooms")
@RestController
@Validated
@Tag(name = "Rooms", description = "Endpoints for managing and querying available rooms")
public class RoomController {
    private static final Logger log = LoggerFactory.getLogger(RoomController.class);

    private final RoomFacade roomFacade;

    public RoomController(final RoomFacade roomFacade) {
        this.roomFacade = roomFacade;
    }

    @Operation(
            summary = "Get available rooms",
            description = "Retrieve all available rooms for a given date range. Supports pagination using 'page', 'size', and 'sort' query parameters."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved available rooms",
                    content = @Content(mediaType = MimeTypeUtils.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PageImpl.class))),
            @ApiResponse(responseCode = "400", description = "Validation error occurred for the date range"),
            @ApiResponse(responseCode = "500", description = "Internal server error occurred")
    })
    @GetMapping(produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    @ConsistentReservationDateParameters
    public Page<RoomDto> getAvailableRooms(
            @Parameter(description = "Start date for room availability (must be today or later)", required = true)
            @FutureOrPresent @RequestParam final LocalDate from,

            @Parameter(description = "End date for room availability (must be in the future)", required = true)
            @Future @RequestParam final LocalDate to,

            @Parameter(description = "Pagination and sorting parameters (page, size, sort)")
            @PageableDefault(size = 10, page = 0) Pageable pageable
    ) {
        if (log.isDebugEnabled()) {
            log.debug("Fetching available rooms from={} to={}, pageable: {}", from, to, pageable);
        }

        // Simulate fetching paginated data
        final List<RoomDto> rooms = roomFacade.getAvailableRooms(from, to, pageable)
                .stream()
                .toList();

        final Page<RoomDto> pagedRooms = new PageImpl<>(rooms, pageable, rooms.size());

        if (log.isDebugEnabled()) {
            log.debug("Available rooms: {}", pagedRooms.getContent());
        }

        return pagedRooms;
    }
}