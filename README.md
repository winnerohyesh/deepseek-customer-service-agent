# DeepSeek 智能客服 Agent

这是一个基于 JDK 17 + Spring Boot 3 + DeepSeek API 的本地可运行智能客服 Agent 应用，满足以下要求：

- 支持多流会话（多 conversation / 多用户）
- 支持知识库（RAG）
- 不使用 AI 框架，仅通过 DeepSeek API 集成大模型
- 具备工程化能力：日志、异常处理、配置、API 设计、可扩展性

## 技术栈

- Java 17
- Spring Boot 3.3.x
- Spring Data JPA
- PostgreSQL
- Maven
- DeepSeek API
- Springdoc OpenAPI

## 运行前提

1. 安装 JDK 17+
2. 安装 PostgreSQL 15+
3. 准备 DeepSeek API Key
4. 确保本机可以访问 DeepSeek API

## 本地启动

### 1）数据库

```bash
docker run -d \
  --name postgres-agent \
  -e POSTGRES_DB=customer_agent \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:15
```

### 2）配置环境变量

```bash
export DEEPSEEK_API_KEY=your_deepseek_api_key
```

### 3）启动应用

```bash
./mvnw clean package
java -jar target/deepseek-customer-agent-0.0.1-SNAPSHOT.jar
```

### 4）访问地址

- Swagger UI: http://localhost:8080/swagger-ui/index.html
- Actuator: http://localhost:8080/actuator/health

## 主要 API

### 创建会话

```http
POST /api/sessions
Content-Type: application/json

{
  "userId": "user-001",
  "title": "订单咨询"
}
```

### 发送消息

```http
POST /api/chat
Content-Type: application/json

{
  "conversationId": "c-xxx",
  "userId": "user-001",
  "message": "我的订单还没发货，怎么处理？"
}
```

### 上传知识库

```http
POST /api/knowledge/import
Content-Type: application/json

{
  "title": "退货政策",
  "category": "policy",
  "content": "在签收后的7天内，符合条件的订单可以申请退货。",
  "sourceUrl": "https://example.com/policy"
}
```

## 项目结构

```text
src/
  main/
    java/
      com/winnerohyesh/customeragent/
        config/
        controller/
        dto/
        entity/
        exception/
        repository/
        service/
        CustomerAgentApplication.java
    resources/
      application.yml
```

## 设计说明

- 会话层：每个 conversation 代表一个独立客服流
- 记录层：保存用户消息和助手回复
- 知识库：支持简单的本地本地召回，适合本地开发与演示
- DeepSeek：仅通过 HTTP API 调用大模型
- 工程化：统一异常、配置管理、日志与 API 文档

## 后续扩展建议

- 接入 PostgreSQL pgvector 实现更强的语义召回
- 添加 Redis 限流与缓存
- 增加消息索引、审计日志、审计可追踪性
- 增加工单转人工与多租户能力
