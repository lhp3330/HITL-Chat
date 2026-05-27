package com.sspu.hitlchat.config;


import io.agentscope.core.formatter.dashscope.DashScopeChatFormatter;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.model.GenerateOptions;
import io.agentscope.core.model.ToolChoice;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class AiConfig {

    @Bean
    public DashScopeChatModel dashScopeChatModel() {
        GenerateOptions options = GenerateOptions.builder()
                .maxTokens(2048)
                .temperature(0.7)
                .topP(0.7)
                .topK(20)
                .toolChoice(new ToolChoice.Auto())
                .build();

        return DashScopeChatModel.builder()
                .modelName(System.getProperty("DASHSCOPE_MODEL"))
                .apiKey(System.getProperty("DASHSCOPE_API_KEY"))
                .defaultOptions(options)
                .formatter(new DashScopeChatFormatter())
                .enableSearch(false)
                .enableThinking(false)
                .enableEncrypt(false)
                .build();
    }
}
