package com.mimacom.lunchandlearn.newapigateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;


/**
 * Routes: http://localhost:8080/actuator/gateway/routes
 * <p>
 * More details here: https://cloud.spring.io/spring-cloud-gateway/multi/multi__actuator_api.html
 */
@Configuration
public class RouteConfigurations {

    @Bean
    RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("order_service_route",
                        route -> route.path("/orders")
                                .and()
                                .method(HttpMethod.GET)
                                .and()
                                .predicate(serverWebExchange ->
                                        serverWebExchange.getRequest().getQueryParams().containsKey("customerId"))
                                .uri("lb://new-order-service")).build();
    }
}
