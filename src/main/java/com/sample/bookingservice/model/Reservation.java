package com.sample.bookingservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

@Data
@Entity
@Table(name = "reservations")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reservationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    @NotNull
    private Room room;

    @Enumerated(EnumType.ORDINAL)
    @NotNull
    private ReservationStatus status = ReservationStatus.INITIAL;

    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    @NotNull
    private LocalDate startDate;

    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    @NotNull
    private LocalDate endDate;

    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    private Date createdAt = new Date();

}
