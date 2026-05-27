package com.sspu.hitlchat.controller;


import com.sspu.hitlchat.dto.ChatEvent;
import com.sspu.hitlchat.dto.ChatRequest;
import com.sspu.hitlchat.dto.McpConfigRequest;
import com.sspu.hitlchat.dto.ToolConfirmRequest;
import com.sspu.hitlchat.service.AgentService;
import com.sspu.hitlchat.service.McpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Set;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ChatController {

    private final AgentService agentService;

    private final McpService mcpService;

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<ChatEvent>> chat(@RequestBody ChatRequest chatRequest) {
        String sessionId = chatRequest.getSessionId();
        String message = chatRequest.getMessage();
        if (!StringUtils.hasText(sessionId)) {
            sessionId = "default";
        }
        sessionId = "1";
        return agentService.chat(sessionId, message)
                .map(v -> ServerSentEvent.<ChatEvent>builder().data(v).build());
    }

    @GetMapping("/tools")
    public ResponseEntity<Set<String>> getTools() {
        return ResponseEntity.ok(agentService.getTools());
    }

    @PostMapping("/chat/interrupt/{sessionId}")
    public ResponseEntity<Map<String, Object>> interrupt(@PathVariable("sessionId") String sessionId) {
        boolean interrupt = agentService.interrupt(sessionId);
        return ResponseEntity.ok(Map.of("success", true, "interrupt", interrupt));
    }

    @GetMapping("/settings/dangerous-tools")
    public ResponseEntity<Set<String>> getDangerousTools() {
        return ResponseEntity.ok(agentService.getConfirmationHook().getDangerousTools());
    }

    @PostMapping("/settings/dangerous-tools")
    public ResponseEntity<Map<String, Object>> setDangerousTools(@RequestBody Set<String> dangerousToolNames) {
        agentService.getConfirmationHook().setDangerousTools(dangerousToolNames);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping(value = "/chat/confirm", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<ChatEvent>> confirmTool(@RequestBody ToolConfirmRequest toolConfirmRequest) {
        String sessionId = toolConfirmRequest.getSessionId();
        List<ToolConfirmRequest.ToolCallInfo> toolCalls = toolConfirmRequest.getToolCalls();
        String reason = toolConfirmRequest.getReason();
        boolean isConfirmed = toolConfirmRequest.isConfirmed();

        if (!StringUtils.hasText(sessionId)) {
            sessionId = "default";
        }
        return agentService.confirmTool(sessionId, isConfirmed, reason, toolCalls)
                .map(v -> ServerSentEvent.<ChatEvent>builder().data(v).build());
    }

    @PostMapping("/mcp/add")
    public Mono<ResponseEntity<Map<String, Object>>> addMcpServer(@RequestBody McpConfigRequest request) {
        return mcpService.addMcpServer(request)
                .then(Mono.just(ResponseEntity.ok(Map.<String, Object>of("success", true))))
                .onErrorResume(e ->
                        Mono.just(ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()))));
    }

    @GetMapping("/mcp/list")
    public ResponseEntity<Set<String>> listMcpServer() {
        return ResponseEntity.ok(mcpService.listMcpServer());
    }

    @DeleteMapping("/mcp/{name}")
    public Mono<ResponseEntity<Map<String, Object>>> deleteMcpServer(@PathVariable("name") String name) {
        return mcpService.deleteMcpServer(name)
                .then(Mono.just(ResponseEntity.ok(Map.<String, Object>of("success", true))))
                .onErrorResume(e ->
                        Mono.just(ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()))));
    }
}
