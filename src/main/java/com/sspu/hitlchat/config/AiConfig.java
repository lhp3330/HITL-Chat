package com.sspu.hitlchat.config;


import io.agentscope.core.formatter.dashscope.DashScopeChatFormatter;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.model.ExecutionConfig;
import io.agentscope.core.model.GenerateOptions;
import io.agentscope.core.model.ToolChoice;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;


@Configuration
public class AiConfig {

    @Bean
    public DashScopeChatModel dashScopeChatModel() {
        ExecutionConfig config = ExecutionConfig.builder()
                .timeout(Duration.ofSeconds(30))
                .maxAttempts(5)
                .build();

        GenerateOptions options = GenerateOptions.builder()
                .maxTokens(2048)
                .temperature(0.7)
                .topP(0.7)
                .topK(20)
                .executionConfig(config)
                .toolChoice(new ToolChoice.Auto())
                .build();

        return DashScopeChatModel.builder()
                .modelName(System.getProperty("DASHSCOPE_MODEL"))
                .apiKey(System.getProperty("DASHSCOPE_API_KEY"))
                .defaultOptions(options)
                .formatter(new DashScopeChatFormatter())
                .stream(true)
                .enableSearch(false)
                .enableThinking(false)
                .enableEncrypt(false)
                .build();
    }
}
