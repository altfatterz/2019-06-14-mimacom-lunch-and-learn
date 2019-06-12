package com.mimacom.lunchandlearn;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@SpringBootApplication
@EnableHystrix
public class CustomerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomerServiceApplication.class, args);
    }

}

@RestController
@RequiredArgsConstructor
class CustomerRestController {

    private final CustomerRepository customerRepository;
    private final OrderServiceClient orderServiceClient;

    @GetMapping("/customers/{customerId}")
    public Customer getCustomer(@PathVariable String customerId) {

        Customer customer = customerRepository.getCustomer(customerId);

        List<Order> orders = orderServiceClient.getOrders(customerId);

        Stream<List<String>> productNamesPerOrder = orders.stream().map(order ->
                order.getItems().stream().map(item -> item.getProductName()).collect(toList()));

        List<String> productNames = productNamesPerOrder.flatMap(productNamesWithinOrder -> productNamesWithinOrder.stream())
                .collect(toList());

        customer.setFavouriteProducts(new HashSet<>(productNames));

        return customer;
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
@RequiredArgsConstructor
@Slf4j
class OrderServiceClient {

    private final RestTemplate restTemplate;

    @HystrixCommand(fallbackMethod = "fallback")
    public List<Order> getOrders(String customerId) {

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
                .fromHttpUrl("http://order-service/orders")
                .queryParam("customerId", customerId);

        ResponseEntity<List<Order>> response = restTemplate.exchange(
                uriComponentsBuilder.toUriString(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Order>>() {});

        return response.getBody();

    }

    public List<Order> fallback(String customerId, Throwable throwable) {
        log.info("Fetching orders with id {}, recovered from {}", customerId, throwable.getMessage());
        return Collections.emptyList();
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
