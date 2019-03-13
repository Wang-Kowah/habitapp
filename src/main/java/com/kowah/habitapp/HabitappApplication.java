package com.kowah.habitapp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@MapperScan("com.kowah.habitapp.dbmapper")
@EnableCaching
public class HabitappApplication {

    public static void main(String[] args) {
        SpringApplication.run(HabitappApplication.class, args);
    }
}