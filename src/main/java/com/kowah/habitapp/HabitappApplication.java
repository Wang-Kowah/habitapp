package com.kowah.habitapp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.kowah.habitapp.dbmapper")
public class HabitappApplication {

    public static void main(String[] args) {
        SpringApplication.run(HabitappApplication.class, args);
    }
}