package com.shedleo.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.shedleo.user", "com.shedleo.common"})
@EnableDiscoveryClient
@EnableFeignClients
public class UserApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
        System.out.println("====================================");
        System.out.println("  Shedleo User Service Started!       ");
        System.out.println("  Port: 8084                         ");
        System.out.println("====================================");
    }
}
