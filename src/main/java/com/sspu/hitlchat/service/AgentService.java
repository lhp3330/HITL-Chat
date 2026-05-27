package com.sspu.hitlchat.service;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sspu.hitlchat.constant.Constants;
import com.sspu.hitlchat.dto.ChatEvent;
import com.sspu.hitlchat.dto.ToolConfirmRequest;
import com.sspu.hitlchat.hook.SafeInputHook;
import com.sspu.hitlchat.hook.ToolConfirmationHook;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.agent.Event;
import io.agentscope.core.agent.StreamOptions;
import io.agentscope.core.message.*;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.session.Session;
import io.agentscope.core.state.SessionKey;
import io.agentscope.core.state.SimpleSessionKey;
import io.agentscope.core.tool.Toolkit;
import jakarta.annotation.Resource;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class AgentService {

    private final Toolkit toolkit;

    private final Session jsonSession;

    private final DashScopeChatModel dashScopeChatModel;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ConcurrentHashMap<String, ReActAgent> runningAgents = new ConcurrentHashMap<>();

    @Getter
    private final ToolConfirmationHook confirmationHook = new ToolConfirmationHook(Set.of("get_time", "list_directory"));

    public ReActAgent createAgent(String sessionId) {
        ReActAgent agent = ReActAgent.builder()
                .name(Constants.AGENT_NAME)
                .sysPrompt(Constants.AGENT_SYS_PROMPT)
                .model(dashScopeChatModel)
                .toolkit(toolkit.copy()) // 保证线程安全
                .hooks(List.of(confirmationHook, new SafeInputHook()))
                .maxIters(10)
                .build();

        runningAgents.put(sessionId, agent);
        agent.loadIfExists(jsonSession, sessionId);
        return agent;
    }

    public Flux<ChatEvent> chat(String sessionId, String message) {
        ReActAgent agent = createAgent(sessionId);

        return agent.stream(Msg.builder().textContent(message).build())
                .flatMap(this::convertEventToChatEvents)
                .concatWith(Flux.just(ChatEvent.complete())) // 添加 COMPLETE 事件
                .doFinally(signal -> {
                    runningAgents.remove(sessionId);
                    agent.saveTo(jsonSession, sessionId);
                })
                // 两条消息：错误信息 + 完成消息
                .onErrorResume(error -> Flux.just(ChatEvent.error(error.getMessage()), ChatEvent.complete()));
    }

    /**
     * 确认工具调用
     * @param sessionId
     * @param confirmed
     * @param reason
     * @param toolCalls
     * @return
     */
    public Flux<ChatEvent> confirmTool(String sessionId,
                                       Boolean confirmed,
                                       String reason,
                                       List<ToolConfirmRequest.ToolCallInfo> toolCalls) {
        ReActAgent agent = createAgent(sessionId);
        if (confirmed) {
            return handleStreamResult(agent, sessionId, agent.stream(StreamOptions.defaults()));
        } else {
            String cancelMsg = StringUtils.hasText(reason) ? reason : Constants.CANCEL_TOOL_MSG;
            ToolResultBlock[] toolResultBlocks = toolCalls.stream()
                    .map(tool -> ToolResultBlock.builder()
                            .id(tool.getId())
                            .name(tool.getName())
                            .output(TextBlock.builder().text(cancelMsg).build())
                            .build()
                    )
                    .toArray(ToolResultBlock[]::new);
            Msg msg = Msg.builder()
                    .role(MsgRole.TOOL)
                    .content(toolResultBlocks)
                    .build();
            return handleStreamResult(agent, sessionId, agent.stream(msg));
        }
    }

    public Set<String> getTools() {
        return toolkit.getToolNames();
    }

    public boolean interrupt(String sessionId) {
        ReActAgent agent = runningAgents.get(sessionId);
        if (agent == null) {
            return false;
        }
        agent.interrupt();
        return true;
    }

    public Set<SessionKey> listSessionKeys() {
        return jsonSession.listSessionKeys();
    }

    public void clearSession(String sessionId) {
        jsonSession.delete(SimpleSessionKey.of(sessionId));
    }

    public boolean existsSession(String sessionId) {
        return jsonSession.exists(SimpleSessionKey.of(sessionId));
    }

    private Flux<ChatEvent> handleStreamResult(ReActAgent agent, String sessionId, Flux<Event> event) {
        return event.flatMap(this::convertEventToChatEvents)
                .concatWith(Flux.just(ChatEvent.complete()))
                .doFinally(signal -> {
                    runningAgents.remove(sessionId);
                    agent.saveTo(jsonSession, sessionId);
                })
                .onErrorResume(error -> Flux.just(ChatEvent.error(error.getMessage()), ChatEvent.complete()));
    }

    private Flux<ChatEvent> convertEventToChatEvents(Event event) {
        List<ChatEvent> events = new ArrayList<>();
        Msg msg = event.getMessage();
        switch (event.getType()) {
            case REASONING -> {
                if (event.isLast() && msg.hasContentBlocks(ToolUseBlock.class)) {
                    List<ToolUseBlock> toolUseBlocks = msg.getContentBlocks(ToolUseBlock.class);
                    boolean hasDangerousTool = toolUseBlocks.stream().anyMatch(t -> confirmationHook.isDangerous(t.getName()));
                    if (hasDangerousTool) {
                        List<ChatEvent.PendingToolCall> pendingToolCalls = toolUseBlocks.stream()
                                .map(t ->
                                        new ChatEvent.PendingToolCall(t.getId(),
                                                t.getName(),
                                                covertToolInput(t.getInput()),
                                                confirmationHook.isDangerous(t.getName()))
                                ).toList();
                        events.add(ChatEvent.toolConfirm(pendingToolCalls));
                    } else {
                        for (ToolUseBlock toolUseBlock : toolUseBlocks) {
                            events.add(ChatEvent.toolUse(toolUseBlock.getId(),
                                    toolUseBlock.getName(),
                                    covertToolInput(toolUseBlock.getInput())));
                        }
                    }
                } else {
                    String content = extraContent(msg);
                    if (StringUtils.hasText(content)) {
                        events.add(ChatEvent.text(content, !event.isLast()));
                    }
                }
            }
            case TOOL_RESULT -> {
                List<ToolResultBlock> toolResultBlocks = msg.getContentBlocks(ToolResultBlock.class);
                for (ToolResultBlock toolUseBlock : toolResultBlocks) {
                    events.add(ChatEvent.toolResult(toolUseBlock.getId(),
                            toolUseBlock.getName(),
                            extraToolOutput(toolUseBlock.getOutput())));
                }
            }
        }
        return Flux.fromIterable(events);
    }

    private Map<String, Object> covertToolInput(Object input) {
        if (input == null) {
            return Map.of();
        }
        if (input instanceof Map<?, ?>) {
            return (Map<String, Object>) input;
        }
        try {
            return objectMapper.convertValue(input, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            return Map.of("value", input.toString());
        }
    }

    private String extraToolOutput(List<ContentBlock> outputs) {
        if (outputs == null || outputs.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (ContentBlock block : outputs) {
            if (block instanceof TextBlock tb) {
                sb.append(tb.getText());
            }
        }
        return sb.toString();
    }

    private String extraContent(Msg msg) {
        List<TextBlock> textBlocks = msg.getContentBlocks(TextBlock.class);
        return Optional.ofNullable(textBlocks)
                .stream()
                .filter(v -> !v.isEmpty())
                .flatMap(List::stream)
                .map(TextBlock::getText)
                .collect(Collectors.joining());
    }
}
