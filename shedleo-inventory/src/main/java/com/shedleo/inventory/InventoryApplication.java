package com.shedleo.inventory;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.shedleo.inventory", "com.shedleo.common"})
@EnableDiscoveryClient
@EnableFeignClients
@EnableScheduling
@MapperScan("com.shedleo.inventory.mapper")
public class InventoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryApplication.class, args);
        System.out.println("======================================");
        System.out.println("  Shedleo Inventory Service Started!  ");
        System.out.println("  Port: 8082                           ");
        System.out.println("======================================");
    }
}
