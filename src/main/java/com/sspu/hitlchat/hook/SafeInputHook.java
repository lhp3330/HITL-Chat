package com.sspu.hitlchat.hook;


import io.agentscope.core.hook.Hook;
import io.agentscope.core.hook.HookEvent;
import io.agentscope.core.hook.PreReasoningEvent;
import io.agentscope.core.message.ContentBlock;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.message.ToolUseBlock;
import reactor.core.publisher.Mono;

import java.util.List;

public class SafeInputHook implements Hook {
    @Override
    public <T extends HookEvent> Mono<T> onEvent(T event) {
        if (event instanceof PreReasoningEvent e) {
            List<Msg> inputMessages = e.getInputMessages();
            for (Msg inputMessage : inputMessages) {
                List<TextBlock> blocks = inputMessage.getContentBlocks(TextBlock.class);
                List<ToolUseBlock> useBlockList = inputMessage.getContentBlocks(ToolUseBlock.class);
                List<ContentBlock> contentBlocks = inputMessage.getContentBlocks(ContentBlock.class);
                contentBlocks.forEach(contentBlock -> {
                    System.out.println(contentBlock.toString());
                });
            }
        }
        return Mono.just(event);
    }

    @Override
    public int priority() {
        return 99;
    }
}
