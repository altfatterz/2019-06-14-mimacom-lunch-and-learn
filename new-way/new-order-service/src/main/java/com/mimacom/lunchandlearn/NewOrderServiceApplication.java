package com.mimacom.lunchandlearn;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class NewOrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NewOrderServiceApplication.class, args);
    }

}

@RestController
class OrderRestController {

    @GetMapping("/orders")
    public List<Order> getOrders(@RequestParam String customerId) {
        return Arrays.asList(
                new Order(new BigDecimal(52.25),
                        Arrays.asList(
                                new OrderItem("Spartan Steak Knife Set", 1),
                                new OrderItem("Retro Pop-Up Hot Dog Toaster", 1),
                                new OrderItem("The Keyboard Waffle Iron", 1))),
                new Order(new BigDecimal(152.25),
                        Arrays.asList(
                                new OrderItem("The Keyboard Waffle Iron", 3),
                                new OrderItem("Paladone Pug Tape Measure", 2))));

    }

}


@Data
@RequiredArgsConstructor
class Order {

    private final BigDecimal price;
    private final List<OrderItem> items;

}

@Data
@RequiredArgsConstructor
class OrderItem {

    private final String productName;
    private final Integer quantity;
}
