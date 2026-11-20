# 小智医疗问诊智能体（xiaozhi-medical-agent）

基于 **Spring Boot 3 + LangChain4j** 构建的医疗问诊 AI Agent：支持**流式多轮对话**、**RAG 医院知识库问答**、**Function Calling 预约挂号**，对话记忆持久化到 MongoDB，业务数据落库 MySQL。

## 核心特性

- **流式对话**：`@AiService` 声明式定义 Agent 接口，基于 Reactor `Flux` 流式返回，对接阿里云百炼 qwen-plus
- **多轮对话记忆**：自定义实现 LangChain4j `ChatMemoryStore` SPI，会话记忆持久化到 MongoDB，服务重启不丢失；10 条滑动窗口控制上下文长度
- **RAG 知识库问答**：医院/科室/神经内科等 md、pdf 文档经 text-embedding-v3 向量化后存入 Pinecone，检索时按 `maxResults` + `minScore` 阈值过滤
- **Function Calling**：`@Tool` 声明预约挂号/取消预约/查询号源三个工具，大模型自主决定调用时机，预约数据经 MyBatis-Plus 写入 MySQL；号源查询基于 `doctor_schedule` 排班表动态计算"容量 - 已约数量"，不是写死的假数据
- **系统提示词变量**：`{{current_date}}` 由 `@V` 参数注入当天日期，"预约日期不得早于今天"等约束真正生效
- **密钥安全**：真实密钥集中存放于本地 `application-secret.yml`，不进 Git、不进 fat jar；仓库只提交模板 `application-secret.example.yml`
- **接口文档**：Knife4j（OpenAPI3），启动后访问 `http://localhost:8080/doc.html`

## 架构

```mermaid
flowchart LR
    U[用户 / 前端] -->|POST /xiaozhi/chat| C[XiaozhiController<br/>Flux 流式返回]
    C --> A[XiaozhiAgent<br/>@AiService 声明式编排]
    A -->|流式生成| M[百炼 qwen-plus<br/>OpenAI 兼容协议]
    A -->|读写会话记忆| MEM[MongoChatMemoryStore<br/>自定义 ChatMemoryStore]
    MEM --> MG[(MongoDB<br/>ai_chat_memory)]
    A -->|检索知识片段| RAG[EmbeddingStoreContentRetriever]
    RAG --> P[(Pinecone 向量库<br/>xiaozhi-index)]
    A -->|工具调用| T[AppointmentTools<br/>@Tool Function Calling]
    T --> DB[(MySQL 8<br/>guiguxiaozhi.appointment)]
    K[knowledge/ md与pdf] -.->|向量化入库| P
```

## 技术栈

| 分类 | 技术 |
| --- | --- |
| 语言 / 构建 | Java 17、Maven（BOM import 统一管理依赖版本） |
| 应用框架 | Spring Boot 3.2.6（Web / Data MongoDB / Test） |
| AI 框架 | LangChain4j 1.0.0-beta3：`@AiService`（EXPLICIT 装配）、`ChatMemoryStore` SPI、`EmbeddingStoreContentRetriever`、`@Tool`、Reactor Flux |
| 大模型服务 | 阿里云百炼 DashScope（qwen-plus 流式对话、text-embedding-v3 向量化）、DeepSeek（OpenAI 兼容接口） |
| RAG | Pinecone（Serverless / AWS us-east-1）、Apache PDFBox 文档解析 |
| 数据存储 | MySQL 8 + MyBatis-Plus 3.5（预约业务）、MongoDB 5（会话记忆） |
| 接口文档 | Knife4j 4.4（OpenAPI3） |

## 快速开始

### 1. 环境准备

- JDK 17+、Maven 3.6+
- MySQL 8（执行 `sql/init.sql` 初始化库表）
- MongoDB（Docker 一键启动，映射到 27018 与 `application.yml` 一致）：

```bash
docker run -d --name xiaozhi-mongo -p 27018:27017 -v xiaozhi-mongo-data:/data/db mongo:5
```

- 大模型 API Key：[阿里云百炼 DashScope](https://bailian.console.aliyun.com/)（对话 + 向量化共用）、[DeepSeek](https://platform.deepseek.com/)、[Pinecone](https://www.pinecone.io/)

### 2. 初始化数据库

```bash
mysql -u root -p < sql/init.sql
```

### 3. 配置密钥

复制密钥模板并填入真实值（两个位置二选一）：

```bash
# 开发（IDE / 源码运行）：放到 classpath
cp src/main/resources/application-secret.example.yml src/main/resources/application-secret.yml

# 生产（fat jar）：放到 jar 同级 config/ 目录（jar 内不含密钥）
cp src/main/resources/application-secret.example.yml config/application-secret.yml
```

> 两个路径均已被 `.gitignore` 忽略，且真实密钥文件不会被打进 fat jar。

### 4. 构建与运行

```bash
mvn clean package -DskipTests
java -jar target/java-ai-langchain4j-1.0-SNAPSHOT.jar
```

### 5. 初始化知识库（首次使用）

激活 ingest 独立运行模式，将 `src/main/resources/knowledge/` 下的文档切分（约 300 字/片段，重叠 30 字）、向量化写入 Pinecone，完成后自动退出：

```bash
java -jar target/java-ai-langchain4j-1.0-SNAPSHOT.jar --spring.profiles.active=ingest
```

正常启动（不带该 profile）不会重复灌库，避免每次启动白白消耗 embedding 调用。

### 6. 开始对话

```bash
curl -X POST http://localhost:8080/xiaozhi/chat \
  -H "Content-Type: application/json" \
  -d '{"memoryId": 1, "message": "帮我挂一个明天上午的神经内科号"}'
```

`memoryId` 为会话标识：同一 memoryId 的多轮对话共享 MongoDB 中的记忆；换一个 memoryId 即开启新会话。

## 项目结构

```
src/main/java/com/shiyi/java/ai/langchain4j
├── XiaozhiApp.java              # 启动类
├── assistant/XiaozhiAgent.java  # Agent 接口（@AiService 声明式编排）
├── controller/XiaozhiController # 对话接口（Flux 流式）
├── config/
│   ├── XiaozhiAgentConfig.java  # 会话记忆、RAG 检索器装配
│   └── EmbeddingStoreConfig.java# Pinecone 向量库装配
├── store/MongoChatMemoryStore.java # 自定义 ChatMemoryStore（MongoDB 持久化）
├── ingest/KnowledgeIngestRunner.java # 知识库向量化入库（ingest profile 独立运行）
├── tools/AppointmentTools.java  # Function Calling 工具（预约/取消/查号源）
├── service/ mapper/ entity/     # 预约与排班持久层（MyBatis-Plus）
└── bean/                        # ChatForm、ChatMessages
src/main/resources
├── knowledge/                   # RAG 知识库（md / pdf）
├── zhaozhi-prompt-template.txt  # 系统提示词模板
├── application.yml              # 主配置（不含密钥）
└── application-secret.example.yml # 密钥模板
```

## 路线图

- [x] 知识库向量化独立 ingest 模式（`--spring.profiles.active=ingest`）+ 显式文档切分
- [x] 号源查询接入排班表，动态计算剩余号量
- [x] 系统提示词注入当前日期（`@V("current_date")`）
- [ ] 对话接口声明 `text/event-stream`，前端 EventSource 无缝对接
- [ ] 接口鉴权（memoryId 与用户绑定）
- [ ] Testcontainers 测试 + GitHub Actions CI

## 说明

本项目为个人学习与求职展示用途，基于 LangChain4j 官方推荐的 Spring Boot 集成方式实现，在其之上完成了密钥外置、fat jar 打包（BOM import 场景下显式声明插件版本）、资源加载兼容（相对路径 + classpath fallback）等工程化改造。
