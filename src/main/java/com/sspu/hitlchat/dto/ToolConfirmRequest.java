package com.sspu.hitlchat.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToolConfirmRequest {

    private String sessionId;

    private boolean confirmed;

    private String reason;

    private List<ToolCallInfo> toolCalls;


    /** Tool call information for rejection response. */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToolCallInfo {
        private String id;
        private String name;

    }
}
