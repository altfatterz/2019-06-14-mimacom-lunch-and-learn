package com.mimacom.lunchandlearn;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@LoadBalancerClient(name = "order-service")
public class WebClientConfiguration {

    @Bean
    @LoadBalanced
    @Qualifier("loadBalancedWebClient")
    WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }

}
