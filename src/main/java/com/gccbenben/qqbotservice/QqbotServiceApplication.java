package com.gccbenben.qqbotservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan("com.gccbenben.qqbotservice.mapper")
@EnableAsync
public class QqbotServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(QqbotServiceApplication.class, args);
    }

}
