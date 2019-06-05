package com.mimacom.lunchandlearn;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
public class NewCustomerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NewCustomerServiceApplication.class, args);
    }

}


@RestController
@RequiredArgsConstructor
class CustomerRestController {

    private final CustomerRepository customerRepository;
    private final OrderServiceClient orderServiceClient;

    @GetMapping("/customers/{customerId}")
    public Mono<Customer> getCustomer(@PathVariable String customerId) {

        Flux<Order> orders = orderServiceClient.getOrder(customerId);

        Flux<List<String>> ps = orders.map(order -> {
            List<String> products = order.getItems().stream().map(item -> item.getProductName()).collect(Collectors.toList());
            return products;
        });



        // all the product names

        return Mono.just(new Customer("John", "Doe",
                LocalDate.of(1983, 02, 10),
                Arrays.asList("foo", "bar")));
    }
}

@Data
@Builder
class Customer {

    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private List<String> products;

}

@Repository
class CustomerRepository {

    public Customer getCustomer(String customerId) {
        return Customer.builder()
                .firstName("John")
                .lastName("Doe")
                .birthDate(LocalDate.of(1983, 2, 10))
                .build();
    }

}

@Component
class OrderServiceClient {

    private final WebClient.Builder webClientBuilder;

    public OrderServiceClient(@Qualifier("loadBalancedWebClient") WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    public Flux<Order> getOrder(String customerId) {
        return webClientBuilder.build().get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("http")
                        .host("new-order-service")
                        .path("/orders")
                        .queryParam("customerId", customerId)
                        .build())
                .retrieve().bodyToFlux(Order.class);
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
