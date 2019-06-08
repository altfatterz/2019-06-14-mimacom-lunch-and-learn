package com.example;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.toList;

@SpringBootApplication
public class NewCustomerServiceV2Application {

    public static void main(String[] args) {
        SpringApplication.run(NewCustomerServiceV2Application.class, args);
    }

}

@RestController
@RequiredArgsConstructor
class CustomerRestController {

    private final CustomerRepository customerRepository;
    private final OrderServiceClient orderServiceClient;

    @GetMapping("/customers/{customerId}")
    public Mono<Customer> getCustomer(@PathVariable String customerId) {

        Customer customer = customerRepository.getCustomer(customerId);

        Flux<Order> orders = orderServiceClient.getOrders(customerId);

        Flux<List<String>> productNamesPerOrder = orders.map(order -> order.getItems().stream().map(item -> item.getProductName()).collect(toList()));

        Mono<List<String>> productNames = productNamesPerOrder.flatMap(strings -> Flux.fromIterable(strings)).collectList();

        return productNames.map(products -> {
            customer.setFavouriteProducts(new HashSet<>(products));
            return customer;
        });
    }
}

@Data
@Builder
class Customer {

    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private Set<String> favouriteProducts;

}

@Repository
class CustomerRepository {

    private static Map<String, Customer> customers = new ConcurrentHashMap<>();

    public CustomerRepository() {
        customers.put("1", Customer.builder()
                .firstName("Walter")
                .lastName("White")
                .birthDate(LocalDate.of(1958, 10, 12))
                .build());
        customers.put("2", Customer.builder()
                .firstName("Jesse")
                .lastName("Pinkman")
                .birthDate(LocalDate.of(1986, 9, 11))
                .build());
    }

    public Customer getCustomer(String customerId) {
        return customers.get(customerId);
    }

}

@Component
@Slf4j
class OrderServiceClient {

    private final WebClient.Builder webClientBuilder;

    public OrderServiceClient(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @CircuitBreaker(name = "orders", fallbackMethod = "fallback")
    public Flux<Order> getOrders(String customerId) {
        return webClientBuilder.build().get().uri(uriBuilder -> uriBuilder
                .scheme("http")
                .host("new-order-service")
                .path("/orders")
                .queryParam("customerId", customerId)
                .build())
                .retrieve().bodyToFlux(Order.class);
    }

    public Flux<Order> fallback(String customerId, Throwable throwable) {
        log.info("Fetching orders with id {}, recovered from {}", customerId, throwable.getMessage());
        return Flux.empty();
    }
}

@Data
class Order {

    private List<OrderItem> items;

}

@Data
class OrderItem {

    private String productName;
}
