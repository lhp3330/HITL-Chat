package com.sspu.hitlchat.tools;


import io.agentscope.core.message.ToolResultBlock;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class BuiltinTools {

    private final Random random = new Random();

    @Tool(name = "get_time", description = "Get the current date and time")
    public ToolResultBlock getTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return ToolResultBlock.text("Current time: " + now.format(formatter));
    }

    @Tool(name = "random_number", description = "Generate a random integer within a specified range")
    public ToolResultBlock randomNumber(
            @ToolParam(name = "min", description = "Minimum value (inclusive)") int min,
            @ToolParam(name = "max", description = "Maximum value (inclusive)") int max) {
        if (min > max) {
            return ToolResultBlock.error("min must be less than or equal to max");
        }
        int result = random.nextInt(max - min + 1) + min;
        return ToolResultBlock.text("Random number between " + min + " and " + max + ": " + result);
    }

}
