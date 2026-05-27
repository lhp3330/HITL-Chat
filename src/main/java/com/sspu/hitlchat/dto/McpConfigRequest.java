package com.sspu.hitlchat.dto;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class McpConfigRequest {

    /**
     * MCP server name (unique identifier).
     */
    private String name;

    /**
     * Transport type: STDIO, SSE, HTTP.
     */
    private String transportType;

    /**
     * For STDIO: command to execute.
     */
    private String command;

    /**
     * For STDIO: command arguments.
     */
    private List<String> args;

    /**
     * For SSE/HTTP: server URL.
     */
    private String url;

    /**
     * HTTP headers for SSE/HTTP transport.
     */
    private Map<String, String> headers;

    /**
     * Query parameters for SSE/HTTP transport.
     */
    private Map<String, String> queryParams;

}