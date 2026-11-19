package com.shiyi.java.ai.langchain4j;

import dev.langchain4j.community.model.dashscope.WanxImageModel;
import dev.langchain4j.data.image.Image;
import dev.langchain4j.http.client.jdk.JdkHttpClientBuilder;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.awt.*;

@SpringBootTest
class LLMTest {

    @Autowired
    OpenAiChatModel qwenChatModel;   // deepseek，由 open-ai starter 自动配置

    @Value("${qwen.base-url}")
    String qwenBaseUrl;
    @Value("${qwen.api-key}")
    String qwenApiKey;
    @Value("${qwen.model-name}")
    String qwenModelName;

    @Test
    void testChat() {
        System.out.println(qwenChatModel.chat("你好"));
    }

    @Test
    void testDashScopeQwen() {
        // 百炼专属域名只提供 OpenAI 兼容接口，用 OpenAiChatModel 而不是 QwenChatModel。
        // 手动构建必须显式指定 httpClientBuilder，否则两套 HTTP 客户端 SPI 冲突。
        OpenAiChatModel qwen = OpenAiChatModel.builder()
                .httpClientBuilder(new JdkHttpClientBuilder())
                .baseUrl(qwenBaseUrl)
                .apiKey(qwenApiKey)
                .modelName(qwenModelName)
                .build();
        System.out.println(qwen.chat("你好"));
    }

    @Test
    public void testDashScopeWanx(){
        WanxImageModel wanxImageModel = WanxImageModel.builder()
                .modelName("wanx2.1-t2i-turbo")
                .apiKey(qwenApiKey)
                .build();
        Response<Image> response = wanxImageModel.generate("奇幻森林精灵：在一片弥漫着轻柔薄雾的 古老森林深处，阳光透过茂密枝叶洒下金色光斑。一位身材娇小、长着透明薄翼的精灵少女站在一朵硕大的蘑菇上。她有着海藻般的绿色长发，发间点缀着蓝色的小花，皮肤泛着珍珠般的微光。身上穿着由翠绿树叶和白色藤蔓编织而成的 连衣裙，手中捧着一颗散发着柔和光芒的水晶球，周围环绕着五彩斑斓的蝴蝶，脚下是铺满苔藓的地面，蘑菇和蕨类植 物丛生，营造出神秘而梦幻的氛围。");
                System.out.println(response.content().url());
    }
}
