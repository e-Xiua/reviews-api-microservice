package com.iwellness.reviews;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ReviewsApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReviewsApiApplication.class, args);
    }
}
