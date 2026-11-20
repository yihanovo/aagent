package com.shiyi.java.ai.langchain4j.ingest;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * 知识库向量化入库的独立运行模式（生产化 ingest）。
 *
 * 默认不生效，只在激活 ingest profile 时执行，避免每次启动都重复
 * 调用 embedding 接口重灌知识库：
 *
 *   java -jar target/java-ai-langchain4j-1.0-SNAPSHOT.jar --spring.profiles.active=ingest
 *
 * 执行流程：加载 knowledge/ 下文档 → 切分为带重叠的片段 → 向量化写入
 * Pinecone → 打印统计后自动退出，不对外提供服务。
 */
@Component
@Profile("ingest")
public class KnowledgeIngestRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeIngestRunner.class);

    /** 相对项目根目录，不再绑定本机用户目录 */
    private static final String KNOWLEDGE_DIR = "src/main/resources/knowledge";

    /** 与检索端一致的知识库文件清单（test.txt 为测试数据，不入生产库） */
    private static final List<String> KNOWLEDGE_FILES = List.of(
            "人工智能.md", "科室信息.md", "神经内科.md", "医院信息.md", "医院信息.pdf");

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final ConfigurableApplicationContext context;

    public KnowledgeIngestRunner(EmbeddingModel embeddingModel,
                                 EmbeddingStore<TextSegment> embeddingStore,
                                 ConfigurableApplicationContext context) {
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
        this.context = context;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("[ingest] 开始知识库向量化入库，共 {} 个文档", KNOWLEDGE_FILES.size());
        List<Document> documents = loadKnowledgeDocuments();

        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                // 显式切分：约 300 字一片、相邻片段重叠 30 字。
                // 不配置切分器时整篇文档会作为一个向量，检索粒度过粗、相似度分数偏低
                .documentSplitter(DocumentSplitters.recursive(300, 30))
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
        ingestor.ingest(documents);

        log.info("[ingest] 完成，已向量化 {} 个文档并写入 Pinecone，应用退出", documents.size());
        System.exit(SpringApplication.exit(context, () -> 0));
    }

    /**
     * 优先按相对项目根目录的路径读取（IDE / Maven 运行时可用）；
     * 打成 fat jar 后资源不在文件系统上，退回 classpath 读取，保证任意环境都能加载。
     */
    private List<Document> loadKnowledgeDocuments() {
        List<Document> documents = new ArrayList<>();
        for (String fileName : KNOWLEDGE_FILES) {
            Path relativePath = Path.of(KNOWLEDGE_DIR, fileName);
            if (Files.isRegularFile(relativePath)) {
                documents.add(loadFromFileSystem(relativePath));
            } else {
                try (InputStream in = new ClassPathResource("knowledge/" + fileName).getInputStream()) {
                    if (fileName.endsWith(".pdf")) {
                        documents.add(new ApachePdfBoxDocumentParser().parse(in));
                    } else {
                        documents.add(Document.from(new String(in.readAllBytes(), StandardCharsets.UTF_8)));
                    }
                } catch (IOException e) {
                    throw new IllegalStateException("无法加载知识库文档: " + fileName, e);
                }
            }
        }
        return documents;
    }

    private Document loadFromFileSystem(Path path) {
        if (path.toString().endsWith(".pdf")) {
            return FileSystemDocumentLoader.loadDocument(path.toString(), new ApachePdfBoxDocumentParser());
        }
        return FileSystemDocumentLoader.loadDocument(path.toString());
    }
}
