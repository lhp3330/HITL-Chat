package com.sspu.hitlchat;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HitlChatApplication {

    public static void main(String[] args) {
        Dotenv.configure().load().entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        SpringApplication.run(HitlChatApplication.class, args);
    }

}
