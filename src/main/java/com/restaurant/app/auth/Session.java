package com.restaurant.app.auth;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity @Table(name = "sessions")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Session {
    @Id
    String token;

    @Column(nullable = false)
    String userEmail;

    @Column(nullable = false)
    Timestamp expiration;
}
