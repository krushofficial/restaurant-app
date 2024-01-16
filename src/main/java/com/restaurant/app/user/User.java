package com.restaurant.app.user;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;

import java.util.*;

@Entity @Table(name = "users")
@Data @Builder(toBuilder = true) @NoArgsConstructor @AllArgsConstructor
public class User {
    @Id
    String email;

    @Column(nullable = false, length = 24)
    String firstName;
    @Column(nullable = false, length = 24)
    String lastName;

    @Column(nullable = false)
    Short zipCode;
    @Column(nullable = false, length = 32)
    String city;
    @Column(nullable = false, length = 64)
    String address;

    @Column(nullable = false)
    String permissionLevel;

    @Column(nullable = false, length = 72)
    String password;

    private static Map<String,Integer> permissionLevelMap = Map.ofEntries(
            Map.entry("user", 0),
            Map.entry("chef", 1),
            Map.entry("admin", 2)
    );
    public boolean hasValidPermissionLevel() {
        return permissionLevelMap.containsKey(permissionLevel);
    }
    public boolean hasPermission(String _reqLevel) {
        int reqLevel = permissionLevelMap.get(_reqLevel);
        int userLevel = permissionLevelMap.get(permissionLevel);
        return userLevel >= reqLevel;
    }
}
