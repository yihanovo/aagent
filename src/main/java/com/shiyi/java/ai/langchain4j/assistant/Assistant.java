package com.shiyi.java.ai.langchain4j.assistant;

import dev.langchain4j.service.spring.AiService;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

@AiService(wiringMode = EXPLICIT, chatModel = "qwenChatModel")  // 指定要注入的 LLM Bean 名称
public interface Assistant {
    String chat(String userMessage);
}
