package com.sspu.hitlchat.config;


import com.sspu.hitlchat.tools.BuiltinTools;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.core.tool.file.ReadFileTool;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ToolConfig {

    @Bean
    public Toolkit toolkit() {
        Toolkit toolkit = new Toolkit();

        toolkit.registerTool(new ReadFileTool());
        toolkit.registerTool(new BuiltinTools());

        return toolkit;
    }
}
