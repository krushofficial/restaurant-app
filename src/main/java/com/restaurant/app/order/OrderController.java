package com.restaurant.app.order;

import com.restaurant.app.menu.MenuItem;
import com.restaurant.app.menu.MenuRepository;
import com.restaurant.app.user.User;
import com.restaurant.app.user.UserRepository;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("order")
public class OrderController {
    private final MenuRepository menuRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public OrderController(MenuRepository menuRepository, OrderRepository orderRepository, UserRepository userRepository) {
        this.menuRepository = menuRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/list")
    public ResponseEntity<List<Order>> getOrders(@RequestAttribute String userEmail) {
        return ResponseEntity.ok(orderRepository.findAllByUserEmail(userEmail));
    }

    @Data public static class NetOrderItem {
        @NotNull
        @Size(min = 1, max = 64)
        String id;

        @NotNull
        Map<String,Boolean> contents = new HashMap<>();
    }

    @Value public static class SendRequest {
        @Digits(integer = 4, fraction = 0)
        @Positive
        Short zipCode;
        @Size(min = 1, max = 32)
        String city;
        @Size(min = 3, max = 64)
        String address;

        @Size(max = 128)
        String extra;

        @NotNull
        @NotEmpty
        List<@NotNull @Valid NetOrderItem> items;
    }

    @PostMapping("/send")
    public ResponseEntity<Void> sendOrder(@RequestAttribute String userEmail, @RequestBody @Valid SendRequest body) {
        Order order = Order.builder()
                .userEmail(userEmail)
                .state("processing")
                .extra(body.extra)
                .build();

        Optional<User> _user = userRepository.findById(userEmail);
        if (_user.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        User user = _user.get();

        if (body.zipCode != null || body.city != null || body.address != null) {
            if (body.zipCode != null && body.city != null && body.address != null) {
                order.zipCode = body.zipCode;
                order.city = body.city;
                order.address = body.address;
            } else {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        } else {
            order.zipCode = user.getZipCode();
            order.city = user.getCity();
            order.address = user.getAddress();
        }

        order.total = 0L;
        order.items = new ArrayList<>();

        for (NetOrderItem netOrderItem : body.items) {
            Optional<MenuItem> _menuItem = menuRepository.findById(netOrderItem.id);
            if (_menuItem.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.GONE);
            }
            MenuItem menuItem = _menuItem.get();

            OrderItem orderItem = OrderItem.builder()
                    .id(menuItem.getId())
                    .niceName(menuItem.getNiceName())
                    .price(menuItem.getPrice())
                    .discount(menuItem.getDiscount())
                    .contents(menuItem.getContents())
                    .build();

            order.total += (long)(Math.ceil(orderItem.price) * (1.0 - (orderItem.discount == null ? 0.0 : orderItem.discount)));

            for (Map.Entry<String,Boolean> entry: netOrderItem.contents.entrySet()) {
                if (orderItem.contents.containsKey(entry.getKey())) {
                    orderItem.contents.put(entry.getKey(), entry.getValue());
                } else {
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
            }

            order.items.add(orderItem);
        }

        orderRepository.save(order);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/list-all")
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderRepository.findAll());
    }

    @Data public static class ModifyRequest {
        @NotNull
        String state;
    }

    @PostMapping("/modify/{id}")
    public ResponseEntity<Void> modifyOrder(@PathVariable Long id, @RequestBody @Valid ModifyRequest body) {
        if (!Arrays.asList(new String[]{"processing", "in delivery", "delivered"}).contains(body.state)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Optional<Order> _order = orderRepository.findById(id);
        if (_order.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Order order = _order.get();

        order.state = body.state;
        orderRepository.save(order);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        Optional<Order> _order = orderRepository.findById(id);
        if (_order.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        orderRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
