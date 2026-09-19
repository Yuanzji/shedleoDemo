package com.shedleo.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.shedleo.payment", "com.shedleo.common"})
@EnableDiscoveryClient
@EnableFeignClients
public class PaymentApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentApplication.class, args);
        System.out.println("====================================");
        System.out.println("  Shedleo Payment Service Started!    ");
        System.out.println("  Port: 8083                         ");
        System.out.println("====================================");
    }
}
