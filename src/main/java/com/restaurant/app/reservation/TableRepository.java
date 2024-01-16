package com.restaurant.app.reservation;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TableRepository extends ListCrudRepository<ReservableTable, Long> {
    List<ReservableTable> findAllByNumberSeatsGreaterThanEqual(long numberSeats);
}
