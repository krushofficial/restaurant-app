package com.restaurant.app.order;

import jakarta.persistence.*;
import lombok.*;

import java.util.*;

@Entity @Table(name = "orders")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String userEmail;
    @Column(nullable = false)
    String state;
    @Column(nullable = false)
    Long total;

    @Column(nullable = false)
    Short zipCode;
    @Column(nullable = false, length = 32)
    String city;
    @Column(nullable = false, length = 64)
    String address;

    @Column(length = 128)
    String extra;

    @ElementCollection
    @Column(nullable = false)
    List<OrderItem> items;
}
