package com.sspu.hitlchat.service;


import com.sspu.hitlchat.dto.McpConfigRequest;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.core.tool.mcp.McpClientBuilder;
import io.agentscope.core.tool.mcp.McpClientWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.sspu.hitlchat.constant.Constants.*;

@Service
@RequiredArgsConstructor
public class McpService {

    private final Toolkit toolkit;

    public static final ConcurrentHashMap<String, McpClientWrapper> mcpClients = new ConcurrentHashMap<>();

    public Mono<Void> addMcpServer(McpConfigRequest request) {
        String name = request.getName();
        if (mcpClients.containsKey(name)) {
            return Mono.error(new IllegalArgumentException("Mcp client with name " + name + " already exists"));
        }
        return buildMcpClient(request)
                .flatMap(mcpClient -> {
                    mcpClients.put(name, mcpClient);
                    return toolkit.registerMcpClient(mcpClient);
                });
    }

    private Mono<McpClientWrapper> buildMcpClient(McpConfigRequest request) {
        String type = request.getTransportType().toUpperCase();
        McpClientBuilder builder = McpClientBuilder.create(request.getName());
        switch (type) {
            case STDIO -> {
                String command = request.getCommand();
                List<String> args = request.getArgs();
                if (args == null || args.isEmpty()) {
                    builder.stdioTransport(command);
                } else {
                    builder.stdioTransport(command, args.toArray(String[]::new));
                }
            }
            case SSE -> {
                builder.sseTransport(request.getUrl());
                addHeaderAndQueryParams(builder, request);
            }
            case HTTP -> {
                builder.streamableHttpTransport(request.getUrl());
                addHeaderAndQueryParams(builder, request);
            }
            default -> {
                return Mono.error(new IllegalArgumentException("Invalid transport type: " + type));
            }
        }
        return builder.buildAsync();
    }

    private void addHeaderAndQueryParams(McpClientBuilder builder, McpConfigRequest request) {
        Map<String, String> headers = request.getHeaders();
        Map<String, String> queryParams = request.getQueryParams();

        if (headers != null && !headers.isEmpty()) {
            headers.forEach(builder::header);
        }

        if (queryParams != null && !queryParams.isEmpty()) {
            queryParams.forEach(builder::queryParam);
        }
    }

    public Set<String> listMcpServer() {
        return mcpClients.keySet();
    }

    public Mono<Void> deleteMcpServer(String name) {
        McpClientWrapper mcp = mcpClients.remove(name);
        if (mcp == null) {
            return Mono.error(new IllegalArgumentException("Mcp client with name " + name + " not found"));
        }
        return toolkit.removeMcpClient(name);
    }
}
