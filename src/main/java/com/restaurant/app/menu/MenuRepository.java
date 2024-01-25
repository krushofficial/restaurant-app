package com.restaurant.app.menu;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuRepository extends ListCrudRepository<MenuItem, String> {
    List<MenuItem> findAllByCategory(String category);
}
