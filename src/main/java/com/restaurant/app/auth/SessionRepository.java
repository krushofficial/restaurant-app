package com.restaurant.app.auth;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionRepository extends CrudRepository<Session, String> {
    Integer deleteAllByUserEmail(String userEmail);
}
