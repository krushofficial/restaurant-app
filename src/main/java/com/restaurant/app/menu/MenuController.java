package com.restaurant.app.menu;

import jakarta.transaction.*;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("menu")
public class MenuController {
    private final MenuRepository menuRepository;

    public MenuController(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    @GetMapping("/list")
    public ResponseEntity<List<MenuItem>> getMenu(@RequestParam Optional<String> category) {
        return category.map(s -> ResponseEntity.ok(menuRepository.findAllByCategory(s))).orElseGet(() -> ResponseEntity.ok(menuRepository.findAll()));
    }

    @Value public static class AddRequest {
        @NotNull
        @Size(min = 1, max = 64)
        String id;

        @NotNull
        @Size(min = 1, max = 64)
        String category;

        @NotNull
        @Size(min = 1, max = 64)
        String niceName;
        @NotNull
        @Size(min = 1, max = 128)
        String description;

        @NotNull
        Long price;
        Float discount;
        @NotNull
        Boolean recommended;

        Map<@NotNull String,@NotNull Boolean> contents;
    }

    @PostMapping("/add")
    public ResponseEntity<Void> addMenuItem(@RequestBody @Valid AddRequest body) {
        MenuItem item = MenuItem.builder()
                .id(body.id)
                .category(body.category)
                .niceName(body.niceName)
                .description(body.description)
                .price(body.price)
                .discount(body.discount)
                .recommended(body.recommended)
                .contents(body.contents)
                .build();
        menuRepository.save(item);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Value public static class ModifyRequest {
        @NotNull
        @Size(min = 1, max = 64)
        String category;

        @NotNull
        @Size(min = 1, max = 64)
        String niceName;
        @NotNull
        @Size(min = 1, max = 128)
        String description;

        @NotNull
        Long price;
        Float discount;
        @NotNull
        Boolean recommended;

        Map<@NotNull String,@NotNull Boolean> contents;
    }

    @PostMapping("/modify/{id}")
    public ResponseEntity<Void> modifyMenuItem(@PathVariable String id, @RequestBody @Valid ModifyRequest body) {
        Optional<MenuItem> _oldItem = menuRepository.findById(id);
        if (_oldItem.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        MenuItem oldItem = _oldItem.get();

        MenuItem item = oldItem.toBuilder()
                .category(body.category)
                .niceName(body.niceName)
                .description(body.description)
                .price(body.price)
                .discount(body.discount)
                .recommended(body.recommended)
                .contents(body.contents)
                .build();

        menuRepository.save(item);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable String id) {
        Optional<MenuItem> _item = menuRepository.findById(id);
        if (_item.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        menuRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Data public static class CategoryRenameRequest {
        @NotNull
        @Size(min = 1, max = 64)
        String newName;
    }

    @PostMapping("/category/rename/{id}")
    public ResponseEntity<Void> renameCategory(@PathVariable String id, @RequestBody @Valid CategoryRenameRequest body) {
        for (MenuItem item : menuRepository.findAllByCategory(id)) {
            item.category = body.newName;
            menuRepository.save(item);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/category/delete/{id}") @Transactional
    public ResponseEntity<Void> deleteCategory(@PathVariable String id) {
        menuRepository.deleteAllByCategory(id);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
