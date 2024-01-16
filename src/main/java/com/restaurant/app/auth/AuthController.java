package com.restaurant.app.auth;

import com.restaurant.app.user.User;
import com.restaurant.app.user.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import org.apache.commons.text.RandomStringGenerator;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

@RestController
@RequestMapping("auth")
public class AuthController {
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;

    private final BCryptPasswordEncoder passwordEncoder;

    private final RandomStringGenerator sessionGenerator;

    public AuthController(UserRepository userRepository, SessionRepository sessionRepository) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;

        this.passwordEncoder = new BCryptPasswordEncoder();

        char[][] sessionCharset = {
                {'0', '9'},
                {'a', 'z'},
                {'A', 'Z'}
        };
        this.sessionGenerator = new RandomStringGenerator.Builder()
                .withinRange(sessionCharset)
                .build();
    }

    @Value public static class LoginRequest {
        @NotNull
        @Email
        String email;

        @NotNull
        @Size(min = 8, max = 64)
        String password;
    }

    @Value public static class LoginResponse {
        String sessionToken;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest body) {
        Optional<User> user = userRepository.findById(body.email);
        if (user.isEmpty() || !this.passwordEncoder.matches(body.password, user.get().getPassword())) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Session session = Session.builder()
                .token(sessionGenerator.generate(64))
                .userEmail(body.email)
                .expiration(new Timestamp(Instant.now().toEpochMilli() + 604_800_000))
                .build();
        sessionRepository.save(session);

        return ResponseEntity.ok(new LoginResponse(session.token));
    }

    @Value public static class RegisterRequest {
        @NotNull
        @Email
        String email;

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
        @Size(min = 8, max = 64)
        String password;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody @Valid RegisterRequest body) {
        User user = User.builder()
                .email(body.email)
                .firstName(body.firstName)
                .lastName(body.lastName)
                .zipCode(body.zipCode)
                .city(body.city)
                .address(body.address)
                .permissionLevel("user")
                .password(this.passwordEncoder.encode(body.password))
                .build();

        if (userRepository.findById(body.email).isPresent()) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        userRepository.save(user);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public Optional<String> isSessionAlive(String token) {
        Optional<Session> _session = sessionRepository.findById(token);
        if (_session.isEmpty()) {
            return Optional.empty();
        }

        Session session = _session.get();
        if (session.expiration.getTime() < Instant.now().toEpochMilli()) {
            return Optional.empty();
        }

        session.setExpiration(new Timestamp(Instant.now().toEpochMilli() + 604_800_000));
        sessionRepository.save(session);

        return Optional.of(session.userEmail);
    }
}
