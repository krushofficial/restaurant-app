package com.restaurant.app.order;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface OrderRepository extends ListCrudRepository<Order, Long> {
    List<Order> findAllByUserEmail(String target);
}
