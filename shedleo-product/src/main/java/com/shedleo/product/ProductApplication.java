package com.shedleo.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.shedleo.product", "com.shedleo.common"})
@EnableDiscoveryClient
@EnableFeignClients
public class ProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductApplication.class, args);
        System.out.println("====================================");
        System.out.println("  Shedleo Product Service Started!    ");
        System.out.println("  Port: 8085                         ");
        System.out.println("====================================");
    }
}
