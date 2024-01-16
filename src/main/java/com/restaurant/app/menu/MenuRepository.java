package com.restaurant.app.menu;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuRepository extends ListCrudRepository<MenuItem, String> {
}
