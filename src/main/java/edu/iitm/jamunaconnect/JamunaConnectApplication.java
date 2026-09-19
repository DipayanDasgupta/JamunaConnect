package edu.iitm.jamunaconnect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * JamunaConnect - hostel information, room lookup and complaint routing.
 * Single Spring Boot process serving server-rendered pages and the REST API.
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class JamunaConnectApplication {

    public static void main(String[] args) {
        SpringApplication.run(JamunaConnectApplication.class, args);
    }
}