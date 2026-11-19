package com.shiyi.java.ai.langchain4j.config;

import dev.langchain4j.http.client.jdk.JdkHttpClientBuilder;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QwenConfig {

    /**
     * 通过 @Bean 注解将 OpenAiChatModel 注册为 Spring 容器中的单例 Bean
     * 方法名 "qwenChatModel" 就是 Bean 的名字（如果不指定的话）
     */
    @Bean
    public OpenAiChatModel qwenChatModel(
            @Value("${qwen.base-url}") String qwenBaseUrl,
            @Value("${qwen.api-key}") String qwenApiKey,
            @Value("${qwen.model-name}") String qwenModelName) {

        return OpenAiChatModel.builder()
                .httpClientBuilder(new JdkHttpClientBuilder())
                .baseUrl(qwenBaseUrl)
                .apiKey(qwenApiKey)
                .modelName(qwenModelName)
                .build();
    }
}