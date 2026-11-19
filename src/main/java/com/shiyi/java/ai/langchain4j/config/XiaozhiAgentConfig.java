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

import java.util.Arrays;
import java.util.List;

/**
 * XiaozhiAgent 专用的聊天记忆配置。
 * Bean 名字必须和 @AiService(chatMemoryProvider = "chatMemoryProviderXiaozhi") 一致，
 * 因为 EXPLICIT 模式下 langchain4j 是按 Bean 名字注入的，不会按类型自动匹配。
 */
@Configuration
public class XiaozhiAgentConfig {

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


    //定义一个 ContentRetriever Bean，用于从知识库中检索相关内容，包含知识库文档的加载、嵌入向量存储和内容检索逻辑，默认将信息向量化后存储在内存中，适合小型知识库
    @Bean
    ContentRetriever contentRetrieverXiaozhi() {
//使用FileSystemDocumentLoader读取指定目录下的知识库文档
//并使用默认的文档解析器对文档进行解析
        Document document1 = FileSystemDocumentLoader.loadDocument("C:\\Users\\fan21\\idea项目\\java-ai-langchain4j\\src\\main\\resources\\knowledge\\人工智能.md");
        Document document2 = FileSystemDocumentLoader.loadDocument("C:\\Users\\fan21\\idea项目\\java-ai-langchain4j\\src\\main\\resources\\knowledge\\科室信息.md");
        Document document3 = FileSystemDocumentLoader.loadDocument("C:\\Users\\fan21\\idea项目\\java-ai-langchain4j\\src\\main\\resources\\knowledge\\神经内科.md");
        List<Document> documents = Arrays.asList(document1, document2, document3);
//使用内存向量存储
        InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
//使用默认的文档分割器,将文档分割成文本片段，并使用默认的嵌入模型将文本片段转换为向量
        EmbeddingStoreIngestor.ingest(documents, embeddingStore);
//从嵌入存储（EmbeddingStore）里检索和查询内容相关的信息
        return EmbeddingStoreContentRetriever.from(embeddingStore);
    }

    @Autowired
    private EmbeddingStore embeddingStore;
    @Autowired
    private EmbeddingModel embeddingModel;
    //定义一个 ContentRetriever Bean，用于从知识库中检索相关内容，包含知识库文档的加载、嵌入向量存储和内容检索逻辑，默认将信息向量化后存储在 Pinecone 向量数据库中，适合大型知识库
    @Bean
    ContentRetriever contentRetrieverXiaozhiPincone() {
// 创建一个 EmbeddingStoreContentRetriever 对象，用于从嵌入存储中检索内容
        return EmbeddingStoreContentRetriever
                .builder()
// 设置用于生成嵌入向量的嵌入模型
                .embeddingModel(embeddingModel)
// 指定要使用的嵌入存储
                .embeddingStore(embeddingStore)
// 设置最大检索结果数量，这里表示最多返回 1 条匹配结果
                .maxResults(1)
// 设置最小得分阈值，只有得分大于等于 0.8 的结果才会被返回
                .minScore(0.8)
// 构建最终的 EmbeddingStoreContentRetriever 实例
                .build();
    }
}
