package com.shiyi.java.ai.langchain4j.controller;

import com.shiyi.java.ai.langchain4j.assistant.WuyouAgent;
import com.shiyi.java.ai.langchain4j.bean.ChatForm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

@Tag(name = "无忧")
@RestController
@RequestMapping("/wuyou")
public class WuyouController {
    @Autowired
    private WuyouAgent wuyouAgent;
    @Operation(summary = "对话")
    @PostMapping("/chat")
    public Flux<String> chat(@RequestBody ChatForm chatForm) {
        // 注入当前日期，填充系统提示词中的 {{current_date}}
        return wuyouAgent.chat(chatForm.getMemoryId(),
                chatForm.getMessage(),
                LocalDate.now().toString());
    }
}
