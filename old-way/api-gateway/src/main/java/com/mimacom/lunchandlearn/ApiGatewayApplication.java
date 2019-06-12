package com.mimacom.lunchandlearn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

@SpringBootApplication
@EnableZuulProxy
@EnableHystrixDashboard // makes the /hystrix available

/**
 * add the following url: http://localhost:9090/actuator/hystrix.stream
 *
 * to make traffic:
 *
 * ab -c 5 -n 5000 http://localhost:8080/customer-service/customers/1
 */

public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

}


