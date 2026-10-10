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
2. 安装 PostgreSQL 15+，或直接使用 docker-compose
3. 准备 DeepSeek API Key
4. 确保本机可以访问 DeepSeek API

## 快速启动

### 方式一：Docker 启动 PostgreSQL

```bash
docker-compose up -d postgres
```

### 方式二：直接启动本地 PostgreSQL

```bash
docker run -d \
  --name postgres-agent \
  -e POSTGRES_DB=customer_agent \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:15
```

### 配置环境变量

```bash
cp .env.example .env
# 编辑 .env，写入真实 DeepSeek Key
source .env
```

### 启动应用

```bash
./mvnw clean package
java -jar target/deepseek-customer-agent-0.0.1-SNAPSHOT.jar
```

### 访问地址

- Swagger UI: http://localhost:8080/swagger-ui/index.html
- Actuator: http://localhost:8080/actuator/health

## 关键 API

### 1. 创建会话

```http
POST /api/sessions
Content-Type: application/json

{
  "userId": "user-001",
  "title": "订单咨询"
}
```

### 2. 发消息

```http
POST /api/chat
Content-Type: application/json

{
  "conversationId": "conv-uuid",
  "userId": "user-001",
  "message": "我的订单还没发货，怎么处理？"
}
```

### 3. 查询会话历史

```http
GET /api/chat/history/{conversationId}
```

### 4. 导入知识库

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

### 5. 查询知识库列表

```http
GET /api/knowledge
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

## 架构说明

- 会话层：每个 conversation 代表一个独立客服流
- 记录层：保存用户消息和助手回复
- 知识库：简单文本分块 + 关键词召回，适合本地开发和演示
- DeepSeek：仅通过 HTTP API 调用大模型
- 工程化：统一异常、配置管理、日志、接口文档

## 后续扩展建议

- 接入 pgvector 做真正的向量语义检索
- 增加 Redis 限流、缓存和幂等控制
- 在消息表中增加 token 统计与审计日志
- 增加工单流转与人工接管能力
- 增加 SSE / WebSocket 实时聊天输出

## 说明

本项目是适合本地开发与演示的工程化骨架，已经具备：

- 多会话能力
- 知识库导入
- DeepSeek API 接入
- 可扩展的 Spring Boot 服务层

如需进一步增强为生产级版本，可继续补齐：

- pgvector 语义召回
- Redis 限流
- Prometheus / Grafana 监控
- 审计日志
- 分布式任务与消息队列
