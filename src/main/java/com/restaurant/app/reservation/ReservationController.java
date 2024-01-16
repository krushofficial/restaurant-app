package com.restaurant.app.reservation;

import com.restaurant.app.user.User;
import com.restaurant.app.user.UserRepository;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("reservation")
public class ReservationController {
    private final ReservationRepository reservationRepository;
    private final TableRepository tableRepository;
    private final UserRepository userRepository;

    public ReservationController(ReservationRepository reservationRepository, TableRepository tableRepository, UserRepository userRepository) {
        this.reservationRepository = reservationRepository;
        this.tableRepository = tableRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/list")
    public ResponseEntity<List<Reservation>> getReservations(@RequestAttribute String userEmail) {
        return ResponseEntity.ok(reservationRepository.findAllByUserEmail(userEmail));
    }

    @Value public static class SendRequest {
        @Size(min = 2, max = 24)
        String firstName;
        @Size(min = 2, max = 24)
        String lastName;

        @NotNull
        Long numberSeats;

        @NotNull
        Timestamp start;
        @NotNull
        Timestamp end;
    }

    @PostMapping("/send")
    public ResponseEntity<Void> sendReservation(@RequestAttribute Optional<String> userEmail, @RequestBody @Valid SendRequest body) {
        long start = body.start.getTime();
        long end = body.end.getTime();
        long curr = Instant.now().toEpochMilli();

        if (end - start < 3_600_000L || end - start > 86_400_000L || start - curr < 86_400_000L || start - curr > 604_800_000L) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Reservation reservation = Reservation.builder()
                .validated(false)
                .start(body.start)
                .end(body.end)
                .build();

        Optional<User> user = Optional.empty();
        if (userEmail.isPresent()) {
            user = userRepository.findById(userEmail.get());
            if (user.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        if (body.firstName != null || body.lastName != null) {
            if (body.firstName != null && body.lastName != null && userEmail.isEmpty()) {
                reservation.firstName = body.firstName;
                reservation.lastName = body.lastName;
            } else {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        } else if (user.isPresent()) {
            reservation.userEmail = userEmail.get();
            reservation.firstName = user.get().getFirstName();
            reservation.lastName = user.get().getLastName();
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        List<ReservableTable> tables = tableRepository.findAllByNumberSeatsGreaterThanEqual(body.numberSeats);
        if (tables.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.GONE);
        }
        List<Long> tableIds = tables.stream().map(ReservableTable::getId).toList();
        List<Reservation> overlappingReservations = reservationRepository.findAllByTableIdInAndEndGreaterThanAndStartLessThan(tableIds, body.start, body.end);
        Map<Long,Boolean> overlappingMap = new HashMap<>();
        for (Reservation _reservation : overlappingReservations) {
            overlappingMap.put(_reservation.tableId, true);
        }
        tables.sort(Comparator.comparing(ReservableTable::getNumberSeats));

        for (ReservableTable table : tables) {
            if (overlappingMap.get(table.id) == null) {
                reservation.tableId = table.id;

                reservationRepository.save(reservation);
                return new ResponseEntity<>(HttpStatus.OK);
            }
        }

        return new ResponseEntity<>(HttpStatus.GONE);
    }

    @GetMapping("/list-all")
    public ResponseEntity<List<Reservation>> getAllReservations() {
        return ResponseEntity.ok(reservationRepository.findAll());
    }

    @PutMapping("/validate/{id}")
    public ResponseEntity<Void> validateReservation(@PathVariable Long id) {
        Optional<Reservation> _reservation = reservationRepository.findById(id);
        if (_reservation.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Reservation reservation = _reservation.get();

        reservation.validated = true;
        reservationRepository.save(reservation);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        Optional<Reservation> _reservation = reservationRepository.findById(id);
        if (_reservation.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        reservationRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/table/list-all")
    public ResponseEntity<List<ReservableTable>> getAllTables() {
        return ResponseEntity.ok(tableRepository.findAll());
    }

    @Data public static class TableUpsertRequest {
        @NotNull
        @Positive
        Long numberSeats;
    }

    @PostMapping("/table/add")
    public ResponseEntity<Void> addTable(@RequestBody @Valid TableUpsertRequest body) {
        ReservableTable table = ReservableTable.builder()
                .numberSeats(body.numberSeats)
                .build();
        tableRepository.save(table);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/table/modify/{id}")
    public ResponseEntity<Void> modifyTable(@PathVariable Long id, @RequestBody @Valid TableUpsertRequest body) {
        Optional<ReservableTable> _oldTable = tableRepository.findById(id);
        if (_oldTable.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        ReservableTable oldTable = _oldTable.get();

        ReservableTable table = oldTable.toBuilder()
                .numberSeats(body.numberSeats)
                .build();

        tableRepository.save(table);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/table/delete/{id}")
    public ResponseEntity<Void> deleteTable(@PathVariable Long id) {
        Optional<ReservableTable> _table = tableRepository.findById(id);
        if (_table.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        tableRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
