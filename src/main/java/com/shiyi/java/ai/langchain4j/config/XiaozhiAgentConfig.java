package com.shiyi.java.ai.langchain4j.config;

import com.shiyi.java.ai.langchain4j.store.MongoChatMemoryStore;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * XiaozhiAgent 专用的聊天记忆与检索配置。
 * Bean 名字必须和 @AiService 中 EXPLICIT 模式指定的名字一致，
 * 因为 EXPLICIT 模式下 langchain4j 是按 Bean 名字注入的，不会按类型自动匹配。
 */
@Configuration
public class XiaozhiAgentConfig {

    /** 相对项目根目录，不再绑定本机用户目录 */
    private static final String KNOWLEDGE_DIR = "src/main/resources/knowledge";
    private static final List<String> KNOWLEDGE_FILES = List.of(
            "人工智能.md", "科室信息.md", "神经内科.md", "医院信息.md","医院信息.pdf");

    @Autowired
    private MongoChatMemoryStore mongoChatMemoryStore;

    //定义一个 ChatMemoryProvider Bean，用于提供聊天记忆存储，支持多轮对话
    @Bean
    ChatMemoryProvider chatMemoryProviderXiaozhi() {
        return memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(10)
                .chatMemoryStore(mongoChatMemoryStore)
                .build();
    }

    /**
     * 内存向量库版本（测试用）。
     * XiaozhiAgent 当前注入的是 contentRetrieverXiaozhiPincone，此 Bean 并未被使用，
     * 因此标记 @Lazy：应用启动时不会加载文档、也不会白白调用一次 embedding 接口。
     * 如需切换回内存版，把 @AiService 的 contentRetriever 改成 contentRetrieverXiaozhi 即可。
     */
    @Bean
    @Lazy
    ContentRetriever contentRetrieverXiaozhi() {
        InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        EmbeddingStoreIngestor.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build()
                .ingest(loadKnowledgeDocuments());
        return EmbeddingStoreContentRetriever.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
    }

    /**
     * 优先按相对项目根目录的路径读取（IDE / Maven 运行时可用）；
     * 打成 fat jar 后资源不在文件系统上，退回 classpath 读取，保证任意环境都能加载。
     * loadKnowledgeDocuments() 方法会被 contentRetrieverXiaozhi() 调用一次，加载知识库文档并生成向量存储。
     */
    private List<Document> loadKnowledgeDocuments() {
        List<Document> documents = new ArrayList<>();
        for (String fileName : KNOWLEDGE_FILES) {
            Path relativePath = Path.of(KNOWLEDGE_DIR, fileName);
            if (Files.isRegularFile(relativePath)) {
                documents.add(FileSystemDocumentLoader.loadDocument(relativePath.toString()));
            } else {
                try (InputStream in = new ClassPathResource("knowledge/" + fileName).getInputStream()) {
                    documents.add(Document.from(new String(in.readAllBytes(), StandardCharsets.UTF_8)));
                } catch (IOException e) {
                    throw new IllegalStateException("无法加载知识库文档: " + fileName, e);
                }
            }
        }
        return documents;
    }

    @Autowired
    private EmbeddingStore embeddingStore;
    @Autowired
    private EmbeddingModel embeddingModel;

    //定义一个 ContentRetriever Bean，使用 Pinecone 向量数据库，适合大型知识库
    @Bean
    ContentRetriever contentRetrieverXiaozhiPincone() {
        return EmbeddingStoreContentRetriever
                .builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .maxResults(1)
                .minScore(0.8)
                .build();
    }
}
