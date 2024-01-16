package com.restaurant.app.reservation;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "tables")
@Data @Builder(toBuilder = true) @NoArgsConstructor @AllArgsConstructor
public class ReservableTable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    Long numberSeats;
}
