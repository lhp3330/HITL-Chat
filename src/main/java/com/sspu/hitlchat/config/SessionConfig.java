package com.sspu.hitlchat.config;


import io.agentscope.core.session.JsonSession;
import io.agentscope.core.session.Session;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
public class SessionConfig {


    @Bean
    public Session jsonSession() {
        Path sessionPath = Path.of("sessions").toAbsolutePath();
//        Path sessionPath = Path.of("E:\\java\\hitl-chat\\src\\main\\resources\\sessions");
        try {
            Files.createDirectories(sessionPath);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create session directory: " + sessionPath, e);
        }
        return new JsonSession(sessionPath);
    }

}
