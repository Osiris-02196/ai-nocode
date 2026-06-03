# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**yu-ai-code-mother** — 零代码应用生成平台 (Zero-code Application Generation Platform). Users describe an app idea in natural language, and AI generates a working application (HTML page, multi-file project, or full Vue project).

This repository contains **two branches**:

| Branch | Architecture | Status |
|--------|-------------|--------|
| `main` | Microservices (Spring Cloud + Dubbo) | Active development |
| `Monomer-branch` | Monolith (Spring Boot) | Experimental / LangGraph4j workflow research |

## Tech Stack

### Microservice Architecture (`main` branch)

- **Spring Boot 3.5.3**, Java 21, Maven
- **Spring Cloud Alibaba 2023.0.1.0** — service governance
- **Apache Dubbo 3.3.0** (triple protocol) — inter-service RPC
- **Nacos** — service registry & discovery
- **MyBatis-Flex 1.11.6** (ORM) + MySQL + HikariCP
- **Redis** (Spring Session, Redisson distributed locks, Caffeine local cache)
- **LangChain4j 1.12.2** — AI integration (streaming chat models, tools, guardrails, AI Services)
- **Project Reactor** — SSE streaming for AI chat responses
- **DashScope SDK** (Alibaba Cloud) — image generation
- **Selenium + WebDriverManager** — app screenshot generation
- **Tencent COS** — object/image storage
- **Knife4j** — API documentation (Swagger UI)
- **Prometheus + Micrometer** — AI model usage monitoring
- **Hutool 5.8.38** — utility library

### Frontend (shared across branches)

- **Vue 3.5**, TypeScript 5.8, Vite 7
- **Ant Design Vue 4** — UI library
- **Pinia** — state management
- **Vue Router** — routing with route-level auth guard
- **Axios** — HTTP client
- **Markdown-it + Highlight.js** — rendering AI responses
- **@umijs/openapi** — auto-generate typed API stubs from OpenAPI spec

## Build & Run Commands

### Backend (microservices)

Build all modules:
```bash
cd yu-ai-code-mother-microservice
mvn clean package -DskipTests
```

Run services (each in its own terminal, start order matters):
```bash
# 1. Start Nacos (service registry) separately

# 2. User service — port 8124
mvn spring-boot:run -pl yu-ai-code-user

# 3. App service — port 8125
mvn spring-boot:run -pl yu-ai-code-app

# 4. Screenshot service
mvn spring-boot:run -pl yu-ai-code-screenshot

# 5. AI service
mvn spring-boot:run -pl yu-ai-code-ai
```

### Frontend
```bash
cd yu-code-mother-frontend

# Install dependencies
npm install

# Start dev server (HMR at http://localhost:5173, proxies /api to respective backends)
npm run dev

# Type-check + build for production
npm run build

# Lint
npm run lint

# Format
npm run format

# Generate API types from OpenAPI spec
npm run openapi2ts
```

### Monomer branch (experimental)
```bash
cd yu-ai-code-mother
./mvnw spring-boot:run
```

## Microservice Architecture

```
yu-ai-code-mother-microservice/          # Maven multi-module (pom)
├── yu-ai-code-common/                   # Shared utilities & cross-cutting
│   ├── annotation/AuthCheck.java        # Role-based auth annotation
│   ├── common/                          # BaseResponse, ResultUtils, PageRequest
│   ├── config/                          # CorsConfig, CosClientConfig, JsonConfig
│   ├── constant/                        # AppConstant, UserConstant
│   ├── exception/                       # BusinessException, ErrorCode, GlobalExceptionHandler
│   ├── generator/MyBatisCodeGenerator   # Code generation utility
│   ├── manager/CosManager.java          # Tencent COS uploads
│   └── utils/                           # CacheKeyUtils, SpringContextUtil
│
├── yu-ai-code-model/                    # Data models (shared, no Spring Boot)
│   ├── entity/                          # App, User, ChatHistory
│   ├── enums/                           # CodeGenTypeEnum, UserRoleEnum, etc.
│   ├── dto/                             # Add/Update/Query/Deploy request DTOs
│   └── vo/                              # AppVO, UserVO, LoginUserVO
│
├── yu-ai-code-client/                   # Dubbo RPC client interfaces
│   ├── InnerScreenshotService.java      # Screenshot RPC interface
│   └── InnerUserService.java            # User RPC interface
│
├── yu-ai-code-user/                     # User service — port 8124
│   ├── controller/UserController.java   # Login, register, user CRUD
│   ├── service/UserService.java         # Business logic
│   ├── mapper/UserMapper.java           # MyBatis-Flex mapper
│   └── aop/AuthInterceptor.java         # Auth check aspect
│
├── yu-ai-code-app/                      # App service — port 8125 (core business)
│   ├── controller/
│   │   ├── AppController.java           # App CRUD, chat/gen SSE, deploy, download
│   │   └── ChatHistoryController.java   # Chat message history
│   ├── service/impl/
│   │   ├── AppServiceImpl.java          # App business logic + AI code gen orchestration
│   │   ├── ChatHistoryServiceImpl.java  # Message persistence
│   │   └── ProjectDownloadServiceImpl.java # ZIP download
│   ├── core/                            # Code generation pipeline
│   │   ├── AiCodeGeneratorFacade.java   # Facade: LLM → parse → save
│   │   ├── parser/                      # HtmlCodeParser, MultiFileCodeParser
│   │   ├── saver/                       # Html/MultiFile file savers
│   │   ├── builder/VueProjectBuilder.java  # npm build
│   │   └── handler/                     # JSON/plain text stream handlers
│   ├── ai/                              # AI service factories
│   │   ├── AiCodeGeneratorServiceFactory.java
│   │   └── AiCodeGenTypeRoutingServiceFactory.java
│   ├── mapper/                          # AppMapper, ChatHistoryMapper
│   ├── ratelimiter/                     # Redisson rate limiting
│   └── aop/AuthInterceptor.java         # Auth check
│
├── yu-ai-code-ai/                       # AI service (pure AI logic)
│   ├── AiCodeGeneratorService.java      # AI code generation interface
│   ├── AiCodeGenTypeRoutingService.java # User prompt → code gen type router
│   ├── config/                          # StreamingChatModelConfig, RoutingAiModelConfig
│   ├── guardrail/                       # PromptSafetyInputGuardrail, RetryOutputGuardrail
│   ├── tools/                           # ToolManager, FileRead/Write/Modify/Delete tools
│   └── model/message/                   # StreamMessage, ToolRequestMessage, etc.
│
├── yu-ai-code-screenshot/               # Screenshot service
│   ├── YuAiCodeScreenshotApplication.java
│   ├── service/ScreenshotService.java
│   └── utils/WebScreenshotUtils.java    # Selenium screenshots
│
└── pom.xml                              # Parent POM (Spring Cloud, Dubbo, common deps)
```

## Key Design Patterns

- **Microservice RPC** — Services communicate via Apache Dubbo (triple protocol) registered in Nacos. The app service calls user service (`InnerUserService`) and screenshot service (`InnerScreenshotService`) through Dubbo client interfaces in `yu-ai-code-client`.
- **Factory Pattern** for AI services — `AiCodeGeneratorServiceFactory` manages per-app AI service instances; `AiCodeGenTypeRoutingServiceFactory` for routing services.
- **Facade Pattern** — `AiCodeGeneratorFacade` orchestrates LLM generation → code parsing → file saving in the app service.
- **Strategy Pattern** — Parsers and savers selected by `CodeGenTypeEnum` via executor classes (HtmlCodeParser vs MultiFileCodeParser).
- **Interceptor Pattern** — `@AuthCheck` annotation with AOP interceptors for role-based access control.
- **Prototype-scoped beans** for AI chat models — each session gets its own model instance.
- **SSE streaming** via Project Reactor `Flux<ServerSentEvent<String>>` for real-time AI response delivery.

## Port Allocation

| Service | Port | Context Path |
|---------|------|-------------|
| User service | 8124 | `/api` |
| App service | 8125 | `/api` |
| Nacos | 8848 | — |
| Prometheus | 9090 | — |

## Important Configuration

- AI models configured per service in `application.yml` (three tiers: chat, streaming, reasoning, routing)
- DeepSeek used for code generation, Qwen-turbo for lightweight routing tasks
- Dubbo RPC uses triple protocol with Nacos registry
- Frontend dev server proxies `/api` → backends (update individual service ports in `vite.config.ts`)
- DB schema: `sql/create_table.sql` (shared across services)
- Session: Redis-backed, 30-day timeout