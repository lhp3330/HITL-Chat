# HITL Chat — 人机协作智能体聊天应用

基于 **Spring Boot 3.5** + **AgentScope Java SDK** 构建的人机协作（Human-in-the-Loop）智能体聊天应用。支持 SSE 流式响应、高危工具拦截确认、MCP 服务器动态管理，以及深色/浅色主题切换。

## 技术栈

| 层级 | 技术 |
|------|------|
| 框架 | Spring Boot 3.5.8 + WebFlux |
| AI 引擎 | AgentScope Java SDK 1.0.12（ReActAgent） |
| 大模型 | 阿里云 DashScope（qwen3.6-plus） |
| 工具协议 | MCP（Model Context Protocol），支持 STDIO / SSE / HTTP |
| 前端 | 原生 HTML + CSS + JS（SSE + marked.js Markdown 渲染） |
| 构建 | Maven，Java 17 |
| 工具库 | Lombok，dotenv-java，Spring AOP |

## 核心特性

- **SSE 流式对话**：Agent 回复以 Server-Sent Events 实时推送，支持打字机效果
- **人机协作（HITL）**：高危工具（如文件读写、Shell 命令）执行前需用户确认，Agent 暂停等待，用户可同意或拒绝
- **MCP 服务器管理**：通过 Web UI 动态添加/删除 MCP 服务器，自动发现并注册远端工具
- **中英文国际化**：前端支持 EN / 中文一键切换
- **Markdown 渲染**：Agent 回复支持完整 Markdown（代码高亮、表格、列表、引用等）
- **深色/浅色主题**：一键切换，自动跟随系统偏好
- **AOP 日志**：所有 API 请求自动记录 URI 和参数

## 项目结构

```
hitl-chat/
├── pom.xml
├── src/main/java/com/sspu/hitlchat/
│   ├── HitlChatApplication.java       # 启动入口，加载 .env
│   ├── aop/
│   │   └── LoggingAspect.java         # 控制器请求日志切面
│   ├── config/
│   │   ├── AiConfig.java              # DashScope 模型配置
│   │   ├── SessionConfig.java         # JsonSession 持久化
│   │   └── ToolConfig.java            # 内置工具注册
│   ├── constant/
│   │   └── Constants.java             # Agent 名称、系统提示词等
│   ├── controller/
│   │   └── ChatController.java        # REST API（聊天/工具/MCP）
│   ├── dto/
│   │   ├── ChatEvent.java             # SSE 事件（TEXT/TOOL_USE/TOOL_RESULT/TOOL_CONFIRM…）
│   │   ├── ChatRequest.java           # 聊天请求
│   │   ├── McpConfigRequest.java      # MCP 服务器配置
│   │   └── ToolConfirmRequest.java    # 工具确认请求
│   ├── hook/
│   │   ├── ToolConfirmationHook.java  # 高危工具拦截钩子
│   │   ├── SafeInputHook.java         # 调试输出钩子
│   │   └── ModelCallHook.java         # 模型调用钩子（预留）
│   ├── service/
│   │   ├── AgentService.java          # Agent 生命周期管理、SSE 流控制
│   │   └── McpService.java            # MCP 客户端增删查、工具注册
│   └── tools/
│       └── BuiltinTools.java          # 内置工具（get_time, random_number）
├── src/main/resources/
│   ├── application.yml                # 服务器配置
│   ├── .env                           # 环境变量（模型/API Key/MCP 地址）
│   └── static/
│       ├── index.html                 # SPA 页面
│       ├── css/style.css              # 完整设计系统（含深色主题）
│       └── js/app.js                  # 前端逻辑（i18n/SSE/Markdown/MCP 管理）
└── src/test/java/.../
    └── HitlChatApplicationTests.java  # 启动测试
```

## 快速开始

### 环境要求

- Java 17+
- Maven 3.6+
- 阿里云 DashScope API Key（[获取地址](https://dashscope.aliyun.com/)）

### 1. 配置环境变量

编辑 `src/main/resources/.env`：

```env
DASHSCOPE_MODEL=<MODEL_NAME>
DASHSCOPE_API_KEY=<YOUR_API_KEY>
```

### 2. 启动应用

```bash
mvn spring-boot:run
# 或
mvn clean package -DskipTests
java -jar target/hitl-chat-0.0.1-SNAPSHOT.jar
```

### 3. 访问

浏览器打开 **http://localhost:8080**

## API 参考

### 聊天接口

| 方法 | 路径 | Content-Type | 说明 |
|------|------|-------------|------|
| POST | `/api/chat` | `text/event-stream` | 发送消息，SSE 流式返回 Agent 响应 |
| POST | `/api/chat/interrupt/{sessionId}` | — | 中断当前正在执行的 Agent |
| POST | `/api/chat/confirm` | `text/event-stream` | 确认或拒绝高危工具调用，恢复 Agent 执行 |

### 工具管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/tools` | 获取当前所有已注册工具 |
| GET | `/api/settings/dangerous-tools` | 获取高危工具列表 |
| POST | `/api/settings/dangerous-tools` | 设置高危工具列表（需确认才能执行） |

### MCP 服务器管理

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/mcp/add` | 添加 MCP 服务器（STDIO/SSE/HTTP） |
| GET | `/api/mcp/list` | 列出已注册的 MCP 服务器 |
| DELETE | `/api/mcp/{name}` | 移除指定 MCP 服务器 |

### SSE 事件类型

| 事件类型 | 说明 |
|----------|------|
| `TEXT` | Agent 文本回复（支持增量流式 `incremental=true`） |
| `TOOL_USE` | Agent 发起工具调用，携带工具名和参数 |
| `TOOL_RESULT` | 工具执行结果返回 |
| `TOOL_CONFIRM` | 高危工具需用户确认，携带 `pendingToolCalls` 列表 |
| `ERROR` | 错误信息 |
| `COMPLETE` | 本轮对话结束 |

## 内置工具

| 工具名 | 说明 | 参数 |
|--------|------|------|
| `get_time` | 获取当前日期时间 | 无 |
| `random_number` | 生成随机整数 | `min`（最小值）, `max`（最大值） |
| `read_file` | 读取文件内容 | 文件路径 |
| `write_file` | 写入文件 | 文件路径 + 内容 |
| `shell_command` | 执行 Shell 命令 | 命令字符串 |

> 默认高危工具（执行前需确认）：`get_time`、`list_directory`

## 人机协作（HITL）工作流程

```
用户发送消息
    ↓
Agent 开始推理（ReActAgent 循环）
    ↓
Agent 决定调用工具
    ↓
┌─ 工具不在高危列表？──→ 直接执行，返回结果给 Agent
│
└─ 工具在高危列表？────→ Agent 暂停，推送 TOOL_CONFIRM 事件到前端
                              ↓
                         用户看到确认/拒绝按钮
                              ↓
                    ┌─ 用户点击「确认」→ 执行工具，Agent 继续推理
                    │
                    └─ 用户点击「拒绝」→ 注入取消消息，Agent 跳过该工具继续
```

核心技术点：
- `ToolConfirmationHook` 实现 AgentScope 的 `Hook` 接口，在 `PostReasoningEvent` 中拦截工具调用
- 调用 `event.stopAgent()` 暂停 Agent 线程
- Agent 线程通过阻塞等待机制挂起，直到 `/api/chat/confirm` 到达才唤醒

## MCP 集成

支持三种传输协议动态接入远端工具服务器：

| 传输类型 | 适用场景 | 示例 |
|----------|---------|------|
| **STDIO** | 本地命令行进程 | `npx -y @modelcontextprotocol/server-filesystem /tmp` |
| **SSE** | HTTP Server-Sent Events | `https://mcp.example.com/sse` |
| **HTTP** | Streamable HTTP | `https://mcp.example.com/mcp` |

通过左侧面板或 API 添加后，MCP 服务器暴露的工具会自动注册到全局 Toolkit，Agent 可直接调用。

## 前端功能

| 功能 | 说明 |
|------|------|
| 流式对话 | SSE 接收，打字机效果实时渲染 |
| Markdown 渲染 | marked.js 解析，支持代码块/表格/引用/列表 |
| 深色模式 | 灰色调暗色主题，跟随系统偏好，localStorage 持久化 |
| 国际化 | 中/英文切换，`data-i18n` 属性驱动 |
| MCP 管理 | 弹窗表单添加/删除 MCP 服务器，支持三种传输协议 |
| 工具管理 | 查看可用工具，勾选设置高危工具列表 |
| 工具确认 | 危险工具调用时内联展示确认/拒绝按钮 |
| 中断对话 | 长对话可随时中断 |

## 配置参考

### application.yml

```yaml
server:
  port: 8080
  servlet:
    encoding:
      charset: UTF-8
      force: true

spring:
  main:
    banner-mode: off
```

### AI 模型参数（AiConfig.java 硬编码）

| 参数 | 值 |
|------|-----|
| maxTokens | 2048 |
| temperature | 0.7 |
| topP | 0.7 |
| topK | 20 |
| timeout | 30s |
| maxRetries | 5 |
| streaming | true |

## 运行流程

1. 启动时加载 `.env` 到系统属性，初始化 Spring 容器
2. `ToolConfig` 注册 5 个内置工具到全局 `Toolkit`
3. `JsonSession` 初始化会话持久化目录（`sessions/`）
4. 用户通过 Web UI 或 API 发送消息
5. `AgentService` 创建 `ReActAgent`，挂载模型 + 全部工具 + `ToolConfirmationHook`
6. Agent 循环推理（最多 10 轮），结果以 `Flux<ChatEvent>` 通过 SSE 推送到前端
7. 遇到高危工具调用 → 暂停 → 用户确认 → 继续
8. 所有 API 请求经 `LoggingAspect` 自动记录日志

## 许可证

Apache License 2.0
