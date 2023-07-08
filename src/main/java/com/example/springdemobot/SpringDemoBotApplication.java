package com.example.springdemobot;

import com.example.springdemobot.controller.MainController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SpringDemoBotApplication {

    public static void main(String[] args) {


        SpringApplication.run(SpringDemoBotApplication.class, args);
    }



}
