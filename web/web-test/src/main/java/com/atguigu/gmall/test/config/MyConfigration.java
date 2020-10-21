package com.atguigu.gmall.test.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyConfigration {
    @Bean
    public String hello() {
        return "welcome to hangge.com";
    }
}
