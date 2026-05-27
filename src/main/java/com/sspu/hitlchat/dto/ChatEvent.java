package com.sspu.hitlchat.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class ChatEvent {

    /** Event type: TEXT, TOOL_USE, TOOL_RESULT, TOOL_CONFIRM, ERROR, COMPLETE. */
    private String type;

    /** Text content for TEXT events. */
    private String content;

    /** Tool name for TOOL_USE/TOOL_RESULT events. */
    private String toolName;

    /** Tool ID for TOOL_USE/TOOL_RESULT events. */
    private String toolId;

    /** Tool input parameters for TOOL_USE events. */
    private Map<String, Object> toolInput;

    /** Tool result for TOOL_RESULT events. */
    private String toolResult;

    /** Pending tool calls for TOOL_CONFIRM events. */
    private List<PendingToolCall> pendingToolCalls;

    /** Error message for ERROR events. */
    private String error;

    /** Indicates if this is incremental content. */
    private boolean incremental;


    public static ChatEvent text(String content, boolean incremental) {
        ChatEvent event = new ChatEvent();
        event.type = "TEXT";
        event.content = content;
        event.incremental = incremental;
        return event;
    }

    public static ChatEvent toolUse(String toolId, String toolName, Map<String, Object> input) {
        ChatEvent event = new ChatEvent();
        event.type = "TOOL_USE";
        event.toolId = toolId;
        event.toolName = toolName;
        event.toolInput = input;
        return event;
    }

    public static ChatEvent toolResult(String toolId, String toolName, String result) {
        ChatEvent event = new ChatEvent();
        event.type = "TOOL_RESULT";
        event.toolId = toolId;
        event.toolName = toolName;
        event.toolResult = result;
        return event;
    }

    public static ChatEvent toolConfirm(List<PendingToolCall> pendingToolCalls) {
        ChatEvent event = new ChatEvent();
        event.type = "TOOL_CONFIRM";
        event.pendingToolCalls = pendingToolCalls;
        return event;
    }

    public static ChatEvent error(String error) {
        ChatEvent event = new ChatEvent();
        event.type = "ERROR";
        event.error = error;
        return event;
    }

    public static ChatEvent complete() {
        ChatEvent event = new ChatEvent();
        event.type = "COMPLETE";
        return event;
    }

    public static ChatEvent interrupted(String message) {
        ChatEvent event = new ChatEvent();
        event.type = "INTERRUPTED";
        event.content = message;
        return event;
    }

    /**
     * Pending tool call information for confirmation.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PendingToolCall {
        private String id;
        private String name;
        private Map<String, Object> input;
        private boolean dangerous;
    }
}
