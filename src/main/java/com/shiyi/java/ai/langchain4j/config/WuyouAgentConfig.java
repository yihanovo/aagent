package com.shiyi.java.ai.langchain4j.config;

import com.shiyi.java.ai.langchain4j.store.MongoChatMemoryStore;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * WuyouAgent 专用的聊天记忆与检索配置。
 * Bean 名字必须和 @AiService 中 EXPLICIT 模式指定的名字一致，
 * 因为 EXPLICIT 模式下 langchain4j 是按 Bean 名字注入的，不会按类型自动匹配。
 *
 * 知识库的向量化入库不在这里做（避免每次启动都重复灌库），
 * 见 ingest/KnowledgeIngestRunner：java -jar app.jar --spring.profiles.active=ingest
 */
@Configuration
public class WuyouAgentConfig {

    @Autowired
    private MongoChatMemoryStore mongoChatMemoryStore;

    @Autowired
    private EmbeddingStore embeddingStore;

    @Autowired
    private EmbeddingModel embeddingModel;

    /** 提供 WuyouAgent 的聊天记忆：每个会话 10 条滑动窗口，底层持久化到 MongoDB */
    @Bean
    ChatMemoryProvider chatMemoryProviderWuyou() {
        return memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(10)
                .chatMemoryStore(mongoChatMemoryStore)
                .build();
    }

    /** Pinecone 向量库版本的检索器，WuyouAgent 实际注入的是这个 Bean */
    @Bean
    ContentRetriever contentRetrieverWuyouPincone() {
        return EmbeddingStoreContentRetriever
                .builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .maxResults(1)
                .minScore(0.8)
                .build();
    }
}
