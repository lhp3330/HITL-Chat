package com.sspu.hitlchat;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.nio.file.Path;


@EnableAspectJAutoProxy
@SpringBootApplication
public class HitlChatApplication {

    public static void main(String[] args) {
        Dotenv.configure().load().entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        SpringApplication.run(HitlChatApplication.class, args);
        System.out.println("----- Open At http://localhost:8080 -----");
    }

}
