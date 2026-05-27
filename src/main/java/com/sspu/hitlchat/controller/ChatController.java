package com.sspu.hitlchat.controller;


import com.sspu.hitlchat.service.AgentService;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.DashScopeChatModel;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ChatController {

    private final AgentService agentService;

    private final DashScopeChatModel dashScopeChatModel;

    @GetMapping("/chat")
    public String test() {
        ReActAgent agent = ReActAgent.builder()
                .model(dashScopeChatModel)
                .build();

        return agent.call(Msg.builder().textContent("你好").build()).block().getTextContent();
    }
}
