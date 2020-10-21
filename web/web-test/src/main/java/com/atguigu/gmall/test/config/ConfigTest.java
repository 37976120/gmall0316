package com.atguigu.gmall.test.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfigTest {
    private String name = "ConfigTest";

    @Bean
    public Myclass mytest() {
        return new Myclass();
    }

    public class Myclass {
        public String name = "MyclassName";
    }

}
