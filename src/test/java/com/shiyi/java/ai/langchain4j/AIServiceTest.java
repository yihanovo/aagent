package com.shiyi.java.ai.langchain4j;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

@SpringBootTest
public class AIServiceTest {
    @Autowired
    OpenAiChatModel qwenChatModel;   // deepseek，由 open-ai starter 自动配置
    @Test
    public void testChat() {

//创建AIService
        Assistant assistant = AiServices.create(Assistant.class, qwenChatModel);
//调用service的接口
        String answer = assistant.chat("你可以做什么");
        System.out.println(answer);
    }

    @Autowired
    private Assistant assistant;
    @Test
    public void testAssistant() {
        String answer = assistant.chat("Hello");
        System.out.println(answer);
    }


    @Test
    public void testChatMemory2() {
//第一轮对话
        UserMessage userMessage1 = UserMessage.userMessage("我是环环");
        ChatResponse chatResponse1 = qwenChatModel.chat(userMessage1);
        AiMessage aiMessage1 = chatResponse1.aiMessage();
//输出大语言模型的回复
        System.out.println("这是ai回复1"+aiMessage1.text());
//第二轮对话
        UserMessage userMessage2 = UserMessage.userMessage("你知道我是谁吗");
        ChatResponse chatResponse2 = qwenChatModel.chat(Arrays.asList(userMessage1,
                aiMessage1, userMessage2));//将第一轮对话的上下文传入第二轮对话，userMessage1,aiMessage1,userMessage2原本是string，aslist将userMessage1,aiMessage1,userMessage2组成一个list传入
        AiMessage aiMessage2 = chatResponse2.aiMessage();
//输出大语言模型的回复
        System.out.println("这是ai回复2"+aiMessage2.text());
    }

    @Test
    public void testChatMemory3() {
//创建chatMemory
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);
//创建AIService
        Assistant assistant = AiServices
                .builder(Assistant.class)
                .chatLanguageModel(qwenChatModel)
                .chatMemory(chatMemory)
                .build();
//调用service的接口
        String answer1 = assistant.chat("我是环环");
        System.out.println(answer1);
        String answer2 = assistant.chat("我是谁");
        System.out.println(answer2);
    }

    @Autowired
    private MemoryChatAssistant memoryChatAssistant;
    @Test
    public void testChatMemory4() {
        String answer1 = memoryChatAssistant.chat("我是环环，只需要回答我你好");
        System.out.println("第一个回答：" + answer1);
        String answer2 = memoryChatAssistant.chat("你是谁，回答你可以干嘛即可");
        System.out.println("第二个回答：" + answer2);
//        String answer3 = memoryChatAssistant.chat("你可以干嘛");
//        System.out.println("第三个回答：" + answer3);
//        String answer4 = memoryChatAssistant.chat("这是废话");
//        System.out.println(answer4);
        String answer5 = memoryChatAssistant.chat("我是谁");
        System.out.println("第四个回答：" + answer5);
    }

    @Autowired
    private SeparateChatAssistant separateChatAssistant;
    @Test
    public void testChatMemory5() {
//        String answer1 = separateChatAssistant.chat(3,"我是环环");
//        System.out.println(answer1);
//        String answer2 = separateChatAssistant.chat(3,"我是谁");
//        System.out.println(answer2);
//        String answer3 = separateChatAssistant.chat(4,"我19了");
//        System.out.println(answer3);
//        String answer4 = separateChatAssistant.chat(4,"你记得我多大吗");
//        System.out.println(answer4);

        String answer = separateChatAssistant.chat1("我是谁");
        System.out.println(answer);
    }
}