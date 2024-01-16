package com.restaurant.app.reservation;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface ReservationRepository extends ListCrudRepository<Reservation, Long> {
    List<Reservation> findAllByUserEmail(String target);

    List<Reservation> findAllByTableIdInAndEndGreaterThanAndStartLessThan(List<Long> tableIds, Timestamp start, Timestamp end);
}
