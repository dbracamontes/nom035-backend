package com.example.nom035;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.example.nom035")
public class Nom035Application {
    public static void main(String[] args) {
        SpringApplication.run(Nom035Application.class, args);
    }
}