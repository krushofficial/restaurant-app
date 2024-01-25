package com.restaurant.app.menu;

import jakarta.persistence.*;
import lombok.*;

import java.util.*;

@Entity @Table(name = "menu")
@Data @Builder(toBuilder = true) @NoArgsConstructor @AllArgsConstructor
public class MenuItem {
    @Id @Column(length = 64)
    String id;

    @Column(nullable = false, length = 64)
    String category;

    @Column(nullable = false, length = 64)
    String niceName;
    @Column(nullable = false, length = 128)
    String description;

    @Column(nullable = false)
    Long price;
    Float discount;
    @Column(nullable = false)
    Boolean recommended;

    @ElementCollection
    @Column(nullable = false)
    Map<String,Boolean> contents;
}
