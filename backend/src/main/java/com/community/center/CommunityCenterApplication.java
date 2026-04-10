package com.community.center;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.community.center.mapper")
@EnableScheduling
public class CommunityCenterApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommunityCenterApplication.class, args);
    }

}