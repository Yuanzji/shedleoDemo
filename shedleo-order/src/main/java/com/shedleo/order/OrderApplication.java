package com.shedleo.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.shedleo.order", "com.shedleo.common"})
@EnableDiscoveryClient
@EnableFeignClients
@EnableScheduling
@MapperScan("com.shedleo.order.mapper")
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
        System.out.println("====================================");
        System.out.println("  Shedleo Order Service Started!     ");
        System.out.println("  Port: 8081                         ");
        System.out.println("====================================");
    }
}
