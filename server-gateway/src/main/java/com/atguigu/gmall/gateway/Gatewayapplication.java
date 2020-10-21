package com.atguigu.gmall.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan({"com.atguigu.gmall"})
@SpringBootApplication
@EnableFeignClients(basePackages = "com.atguigu.gmall")
@EnableDiscoveryClient
public class Gatewayapplication {
    public static void main(String[] args) {
        SpringApplication.run(Gatewayapplication.class, args);
    }
}
