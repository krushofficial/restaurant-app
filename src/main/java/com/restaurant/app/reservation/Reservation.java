package com.restaurant.app.reservation;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity @Table(name = "reservations")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Reservation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    Boolean validated;

    @Column(nullable = false)
    Long tableId;
    String userEmail;

    @Column(nullable = false, length = 24)
    String firstName;
    @Column(nullable = false, length = 24)
    String lastName;

    @Column(nullable = false)
    Timestamp start;
    @Column(nullable = false)
    Timestamp end;
}
