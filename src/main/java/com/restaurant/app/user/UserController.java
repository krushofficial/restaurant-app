package com.restaurant.app.user;

import com.restaurant.app.auth.SessionRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("user")
public class UserController {
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;

    public UserController(SessionRepository sessionRepository, UserRepository userRepository) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
    }

    @Builder
    @Value public static class InfoResponse {
        String email;

        String firstName;
        String lastName;

        Short zipCode;
        String city;
        String address;

        String permissionLevel;
    }

    private static InfoResponse userToInfo(User user) {
        return InfoResponse.builder()
                .email(user.email)
                .firstName(user.firstName)
                .lastName(user.lastName)
                .zipCode(user.zipCode)
                .city(user.city)
                .address(user.address)
                .permissionLevel(user.permissionLevel)
                .build();
    }

    @GetMapping("/info")
    public ResponseEntity<InfoResponse> getInfo(@RequestAttribute String userEmail) {
        Optional<User> _user = userRepository.findById(userEmail);
        if (_user.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        User user = _user.get();

        return ResponseEntity.ok(userToInfo(user));
    }

    @GetMapping("/list-all")
    public ResponseEntity<List<InfoResponse>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll().stream().map(UserController::userToInfo).collect(Collectors.toList()));
    }

    @Value public static class ModifyRequest {
        @NotNull
        @Size(min = 2, max = 24)
        String firstName;
        @NotNull
        @Size(min = 2, max = 24)
        String lastName;

        @NotNull
        @Digits(integer = 4, fraction = 0)
        @Positive
        Short zipCode;
        @NotNull
        @Size(min = 1, max = 32)
        String city;
        @NotNull
        @Size(min = 3, max = 64)
        String address;

        @NotNull
        String permissionLevel;
    }

    @PostMapping("/modify/{email}")
    public ResponseEntity<Void> modifyUser(@PathVariable String email, @RequestBody @Valid ModifyRequest body) {
        Optional<User> _oldUser = userRepository.findById(email);
        if (_oldUser.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        User oldUser = _oldUser.get();

        User user = oldUser.toBuilder()
                .email(email)
                .firstName(body.firstName)
                .lastName(body.lastName)
                .zipCode(body.zipCode)
                .city(body.city)
                .address(body.address)
                .permissionLevel(body.permissionLevel)
                .build();
        if (!user.hasValidPermissionLevel()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        userRepository.save(user);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/delete/{email}") @Transactional
    public ResponseEntity<Void> deleteUser(@PathVariable String email) {
        Optional<User> _user = userRepository.findById(email);
        if (_user.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        userRepository.deleteById(email);
        sessionRepository.deleteAllByUserEmail(email);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
