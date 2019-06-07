package com.mimacom.lunchandlearn;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.circuitbreaker.commons.CircuitBreaker;
import org.springframework.cloud.circuitbreaker.commons.CircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.commons.Customizer;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.toList;

@SpringBootApplication
public class NewCustomerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NewCustomerServiceApplication.class, args);
    }

    @Bean
    Customizer<Resilience4JCircuitBreakerFactory> defaultCustomizer() {
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .timeLimiterConfig(TimeLimiterConfig.custom()
                        .timeoutDuration(Duration.ofSeconds(2))
                        .build())
                .circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
                .build());
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
class OrderServiceClient {

    private final WebClient.Builder webClientBuilder;
    private final CircuitBreakerFactory circuitBreakerFactory;
    private final Timer getOrdersTimer;

    public OrderServiceClient(@Qualifier("loadBalancedWebClient") WebClient.Builder webClientBuilder,
                              CircuitBreakerFactory circuitBreakerFactory,
                              MeterRegistry meterRegistry) {
        this.webClientBuilder = webClientBuilder;
        this.circuitBreakerFactory = circuitBreakerFactory;
        this.getOrdersTimer = meterRegistry.timer("get-orders");
    }

    public Flux<Order> getOrders(String customerId) {
        Flux<Order> orders = webClientBuilder.build().get().uri(uriBuilder -> uriBuilder
                .scheme("http")
                .host("new-order-service")
                .path("/orders")
                .queryParam("customerId", customerId)
                .build())
                .retrieve().bodyToFlux(Order.class);

        return orders.transform(it -> {
            CircuitBreaker cb = circuitBreakerFactory.create("get-orders");
            return cb.run(() -> it);
        });


//        return circuitBreakerFactory.create("get-orders").run(
//                () -> {
//                    System.out.println(" !!! calling the order-service !!! ");
//                    return webClientBuilder.build().get().uri(uriBuilder -> uriBuilder
//                            .scheme("http")
//                            .host("new-order-service")
//                            .path("/orders")
//                            .queryParam("customerId", customerId)
//                            .build())
//                            .retrieve().bodyToFlux(Order.class);
//                },
//                throwable -> {
//                    System.out.println(" !!! I was here !!!!");
//                    return Flux.empty();
//                });

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
