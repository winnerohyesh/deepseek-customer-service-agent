# DeepSeek 智能客服 Agent

这是一个基于 JDK 17 + Spring Boot 3 + DeepSeek API 的智能客服 Agent 应用，适合本地开发、演示和二次开发。

## 功能概览

- 支持多会话（多 conversation / 多用户）
- 支持消息持久化
- 支持知识库导入与召回
- 支持 DeepSeek API 调用
- 支持普通聊天和流式聊天（SSE）
- 支持接口文档（Swagger）
- 支持本地 PostgreSQL 数据库
- 提供 Vue 3 前端 Demo

## 技术栈

- Java 17
- Spring Boot 3.3.x
- Spring Data JPA
- PostgreSQL
- Maven
- DeepSeek API
- Springdoc OpenAPI
- Vue 3 + TypeScript + Vite

## 目录结构

```text
.
├── docker-compose.yml
├── .env.example
├── README.md
├── pom.xml
├── mvnw
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── winnerohyesh/
│       │           └── customeragent/
│       │               ├── config/
│       │               ├── controller/
│       │               ├── dto/
│       │               ├── entity/
│       │               ├── exception/
│       │               ├── repository/
│       │               ├── service/
│       │               └── CustomerAgentApplication.java
│       └── resources/
│           ├── application.yml
│           ├── application-dev.yml
│           ├── application-prod.yml
│           └── db/
│               └── V1__init.sql
├── frontend/
│   ├── package.json
│   ├── vite.config.ts
│   ├── index.html
│   ├── tsconfig.json
│   ├── tsconfig.node.json
│   └── src/
│       ├── App.vue
│       ├── main.ts
│       └── api/
│           └── client.ts
└── .gitignore
```

## 环境要求

- JDK 17+
- Maven 3.8+
- Node.js 18+
- Docker（可选，用于启动 PostgreSQL）

## 本地启动

### 1）启动 PostgreSQL

最简单方式：

```bash
docker-compose up -d postgres
```

如果你不想用 Docker，也可手动安装 PostgreSQL，并使用以下默认连接参数：

- host: localhost
- port: 5432
- database: customer_agent
- username: postgres
- password: postgres

### 2）配置环境变量

在项目根目录执行：

```bash
cp .env.example .env
```

编辑 `.env`：

```bash
DEEPSEEK_API_KEY=your_deepseek_api_key_here
```

### 3）启动后端

```bash
bash mvnw clean package
java -jar target/deepseek-customer-agent-0.0.1-SNAPSHOT.jar
```

如果系统里已经安装 Maven：

```bash
mvn clean package
java -jar target/deepseek-customer-agent-0.0.1-SNAPSHOT.jar
```

### 4）启动前端

```bash
cd frontend
npm install
npm run dev
```

前端默认访问地址：

- http://localhost:5173

后端默认访问地址：

- Swagger UI: http://localhost:8080/swagger-ui/index.html
- Actuator: http://localhost:8080/actuator/health

## 环境切换

默认配置：

```bash
spring:
  profiles:
    active: dev
```

或者手动设置：

```bash
SPRING_PROFILES_ACTIVE=dev java -jar target/deepseek-customer-agent-0.0.1-SNAPSHOT.jar
```

生产环境：

```bash
SPRING_PROFILES_ACTIVE=prod java -jar target/deepseek-customer-agent-0.0.1-SNAPSHOT.jar
```

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

### 2. 发送消息

```http
POST /api/chat
Content-Type: application/json

{
  "conversationId": "xxx-uuid",
  "userId": "user-001",
  "message": "我的订单还没发货，怎么处理？"
}
```

### 3. 流式聊天

```http
GET /api/chat/stream?conversationId=xxx-uuid&userId=user-001&message=我的订单还没发货，怎么处理？
```

### 4. 查询消息历史

```http
GET /api/chat/history/{conversationId}
```

### 5. 导入知识库

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

### 6. 查询知识库列表

```http
GET /api/knowledge
```

## 数据库表说明

### conversations

- id
- conversation_id
- user_id
- title
- status
- created_at
- updated_at

### chat_messages

- id
- conversation_id
- role
- content
- created_at

### knowledge_documents

- id
- title
- category
- content
- source_url
- created_at
- updated_at

### knowledge_chunks

- id
- document_id
- content
- chunk_index
- created_at

## 前端说明

前端应用为一个轻量级聊天 Demo，界面包括：

- 会话列表
- 当前会话消息
- 用户输入区
- 知识库列表
- 知识库导入弹窗

默认前端 API 地址：

```bash
http://localhost:8080
```

## 设计说明

### 1. 会话层
每个 conversation 代表一个独立对话流，允许同一用户创建多个会话。

### 2. 消息层
用户消息和 AI 回复都记录到数据库中，便于未来做审计、对话分析和恢复。

### 3. 知识库层
知识库文本会被拆分为多个 chunk，然后在对话时进行匹配和注入，以支持知识增强型回答。

### 4. AI 层
通过 DeepSeek Chat API 进行回答生成，简单且可扩展。

## 常见问题排查

### 1. 后端启动失败
原因：
- Java 版本不对
- PostgreSQL 未启动
- DeepSeek API Key 为空

解决：
- 确认 JDK 17
- 检查数据库状态
- 检查 `.env`

### 2. 连接 PostgreSQL 失败
原因：
- 5432 端口被占用
- 用户名、密码或数据库名不一致

解决：
- 检查 `docker-compose.yml`
- 检查 `application.yml`
- 确认数据库已创建

### 3. DeepSeek 返回失败
原因：
- API Key 错误
- 网络访问受限
- 模型名错误

解决：
- 检查 `deepseek.api-key`
- 确认网络连通性
- 验证 DeepSeek 账号权限

### 4. 前端无法访问后端
原因：
- 后端未启动
- 前端代理配置不对
- 端口冲突

解决：
- 确认后端启动成功
- 检查 `frontend/vite.config.ts`
- 确认本地 5173 和 8080 端口可用

## 后续扩展建议

- 接入 pgvector 做真正的语义搜索
- 接入 Redis 做缓存与限流
- 增加用户认证与权限控制
- 增加工单系统与人工接管
- 增加审计日志和 token 使用统计
- 增加多租户与角色管理
- 增强前端 UI 交互和消息流展示
- 增加 WebSocket / SSE 统一消息实时通道

## 结论

这个项目已经具备：

- 智能客服 Agent 的基础后端能力
- DeepSeek AI 接入能力
- 知识库支持能力
- 前端 Demo 层
- 本地开发和演示所需的工程化结构

它非常适合作为：

- 智能客服原型
- DeepSeek 集成 Demo
- 本地开发学习项目
- 后续扩展为真实生产环境的基础架构

如果你后续还想提升这个项目，我建议下一步直接做：

- pgvector 语义召回
- Redis 限流
- 通用认证体系
- 更成熟的前端界面

这样就可以从 Demo 逐渐升级成生产级智能客服 Agent 平台。
