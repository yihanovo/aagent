package com.shiyi.java.ai.langchain4j;

import dev.langchain4j.community.model.dashscope.QwenTokenizer;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.onnx.HuggingFaceTokenizer;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class RAGTest {

    // 模板与知识库都改用相对项目根目录的路径，不再绑定本机用户目录
    private static final String PROMPT_TEMPLATE = "src/main/resources/my-prompt-template.txt";
    private static final String KNOWLEDGE_PDF = "src/main/resources/knowledge/医院信息.pdf";

    @Test
    public void testReadDocument() {
//使用FileSystemDocumentLoader读取指定目录下的知识库文档
//并使用默认的文档解析器TextDocumentParser对文档进行解析
        Document document = FileSystemDocumentLoader.loadDocument(PROMPT_TEMPLATE);
                System.out.println(document.text());
    }

    @Test
    public void testParsePDF() {
        Document document = FileSystemDocumentLoader.loadDocument(
                KNOWLEDGE_PDF,
                new ApachePdfBoxDocumentParser()
        );
        System.out.println(document);
    }

    @Test
    public void testReadDocumentAndStore() {
//使用FileSystemDocumentLoader读取指定目录下的知识库文档
//并使用默认的文档解析器对文档进行解析(TextDocumentParser)
        Document document = FileSystemDocumentLoader.loadDocument(PROMPT_TEMPLATE);
//为了简单起见，我们暂时使用基于内存的向量存储
        InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
//ingest
//1、分割文档：默认使用递归分割器，将文档分割为多个文本片段，每个片段包含不超过 300个token，并且有 30个token的重叠部分保证连贯性
// DocumentByParagraphSplitter(DocumentByLineSplitter(DocumentBySentenceSplitter(DocumentByWordSplitter)))
//2、文本向量化：使用一个LangChain4j内置的轻量化向量模型对每个文本片段进行向量化
//3、将原始文本和向量存储到向量数据库中(InMemoryEmbeddingStore)
        EmbeddingStoreIngestor.ingest(document, embeddingStore);
//查看向量数据库内容
        System.out.println(embeddingStore);
    }

    @Test
    public void testDocumentSplitter() {
//使用FileSystemDocumentLoader读取指定目录下的知识库文档
//并使用默认的文档解析器对文档进行解析(TextDocumentParser)
        Document document = FileSystemDocumentLoader.loadDocument(PROMPT_TEMPLATE);
//为了简单起见，我们暂时使用基于内存的向量存储
        InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
//自定义文档分割器
//按段落分割文档：每个片段包含不超过 300个token，并且有 30个token的重叠部分保证连贯性
//注意：当段落长度总和小于设定的最大长度时，就不会有重叠的必要。
        DocumentByParagraphSplitter documentSplitter = new DocumentByParagraphSplitter(300, 30,
//token分词器：按token计算
                new HuggingFaceTokenizer());
        EmbeddingStoreIngestor
                .builder()
                .embeddingStore(embeddingStore)
                .documentSplitter(documentSplitter)
                .build()
                .ingest(document);
    }
//按字符计算
//DocumentByParagraphSplitter documentSplitter = new DocumentByParagraphSplitter(300, 30);

    @Value("${qwen.api-key}")
    String qwenApiKey;

    @Test
    public void testTokenCount() {
        String text = "这是一个示例文本，用于测试 token 长度的计算。";
        UserMessage userMessage = UserMessage.userMessage(text);
//计算 token 长度
QwenTokenizer tokenizer1 = new QwenTokenizer(qwenApiKey, "qwen-max");
        HuggingFaceTokenizer tokenizer = new HuggingFaceTokenizer();
        int count = tokenizer1.estimateTokenCountInMessage(userMessage);
        System.out.println("token长度：" + count);
    }
}
