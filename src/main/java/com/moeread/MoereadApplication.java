package com.moeread;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.moeread.mapper")
public class MoereadApplication {

    public static void main(String[] args) {
        SpringApplication.run(MoereadApplication.class, args);
    }
}
