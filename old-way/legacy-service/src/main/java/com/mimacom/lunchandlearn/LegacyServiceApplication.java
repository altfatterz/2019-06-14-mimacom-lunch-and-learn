package com.mimacom.lunchandlearn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class LegacyServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LegacyServiceApplication.class, args);
    }

}

@RequestMapping("/legacy")
@RestController
class LegacyServiceController {

    @GetMapping
    String ignored() {
        return "Not exposed through api-gateway";
    }

    @GetMapping("/exposed")
    String allowed() {
        return "Exposed through api-gateway";
    }

}