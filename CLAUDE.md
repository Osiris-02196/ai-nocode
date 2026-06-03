# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**yu-ai-code-mother** — 零代码应用生成平台 (Zero-code Application Generation Platform). Users describe an app idea in natural language, and AI generates a working application (HTML page, multi-file project, or full Vue project).

## Tech Stack

### Backend
- **Java 21**, Spring Boot 3.5.12, Maven
- **MyBatis-Flex 1.11.6** (ORM) + MySQL + HikariCP
- **Redis** (Spring Session, Redisson distributed locks, Caffeine local cache)
- **LangChain4j 1.12.2** — AI integration (streaming chat models, tools, guardrails, AI Services)
- **LangGraph4j 1.6.0** — Workflow orchestration (state graphs, conditional edges, concurrent nodes)
- **Project Reactor** — SSE streaming for AI chat responses
- **DashScope SDK** (Alibaba Cloud) — image generation
- **Selenium + WebDriverManager** — app screenshot generation
- **Tencent COS** — object/image storage
- **Knife4j** — API documentation (Swagger UI)
- **Prometheus + Micrometer** — AI model usage monitoring

### Frontend
- **Vue 3.5**, TypeScript 5.8, Vite 7
- **Ant Design Vue 4** — UI library
- **Pinia** — state management
- **Vue Router** — routing with route-level auth guard
- **Axios** — HTTP client
- **Markdown-it + Highlight.js** — rendering AI responses

## Build & Run Commands

### Backend
```bash
# Build the entire project (skip tests for speed)
./mvnw clean package -DskipTests

# Run tests
./mvnw test

# Run a specific test class
./mvnw test -Dtest=AiCodeGeneratorServiceTest

# Run a single test method
./mvnw test -Dtest=AiCodeGeneratorServiceTest#testGenerateHtmlCode

# Start the backend server (uses application.yml + application-local.yml)
./mvnw spring-boot:run
```

### Frontend
```bash
cd yu-code-mother-frontend

# Install dependencies
npm install

# Start dev server (HMR at http://localhost:5173, proxies /api to :8123)
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

## Project Architecture

```
yu-ai-code-mother/
├── src/main/java/com/oxiris/yuaicodemother/
│   ├── YuAiCodeMotherApplication.java          # Entry point
│   ├── ai/                                      # AI code generation services
│   │   ├── AiCodeGeneratorService.java          # Interface for code gen
│   │   ├── AiCodeGenTypeRoutingService.java     # Routes user prompt → code gen type
│   │   ├── AiCodeGeneratorServiceFactory.java   # Factory (supports multi-instance, one per app)
│   │   ├── guardrail/                           # Prompt safety + retry guardrails
│   │   ├── model/                               # HtmlCodeResult, MultiFileCodeResult, message types
│   │   └── tools/                               # ToolManager + file operation tools (read/write/modify/delete/dir)
│   ├── core/                                     # Code generation pipeline
│   │   ├── AiCodeGeneratorFacade.java            # Facade — orchestrates generation + save
│   │   ├── parser/                               # HtmlCodeParser, MultiFileCodeParser, CodeParserExecutor
│   │   ├── saver/                                # HtmlCodeFileSaverTemplate, MultiFileCodeFileSaverTemplate, CodeFileSaverExecutor
│   │   ├── builder/VueProjectBuilder.java        # Runs npm build for Vue projects
│   │   └── handler/                              # Stream handlers (JSON, plain text)
│   ├── langgraph4j/                              # LangGraph4j workflow
│   │   ├── CodeGenWorkflow.java                  # Full workflow: image → enhance → route → gen → quality check → build
│   │   ├── CodeGenConcurrentWorkflow.java        # Parallel image collection workflow
│   │   ├── node/                                 # Nodes: PromptEnhancer, Router, CodeGenerator, CodeQualityCheck, etc.
│   │   ├── node/concurrent/                      # Concurrent nodes: LogoCollector, DiagramCollector, etc.
│   │   ├── state/WorkflowContext.java            # Shared workflow state
│   │   ├── tools/                                # Tools: ImageSearch, LogoGenerator, MermaidDiagram, UndrawIllustration
│   │   └── ai/                                   # LangGraph-specific AI services (code quality, image collection)
│   ├── controller/                               # REST controllers
│   │   ├── AppController.java                    # CRUD, chat/gen SSE stream, deploy, download
│   │   ├── UserController.java                   # Login, register, user management
│   │   ├── ChatHistoryController.java            # Chat message history
│   │   ├── HealthController.java                 # Health check
│   │   └── StaticResourceController.java         # Serve generated static files
│   ├── service/ + service/impl/                  # Business service layer
│   ├── mapper/                                   # MyBatis-Flex mappers
│   ├── model/entity/                             # App, User, ChatHistory entities
│   ├── model/enums/                              # CodeGenTypeEnum (HTML/MULTI_FILE/VUE_PROJECT), UserRoleEnum, etc.
│   ├── model/dto/                                # App/User/ChatHistory DTOs
│   ├── model/vo/                                 # View objects (AppVO, UserVO, LoginUserVO)
│   ├── config/                                   # Spring configs
│   │   ├── StreamingChatModelConfig.java         # Prototype-scoped streaming AI model
│   │   ├── RoutingAiModelConfig.java             # AI model for type routing (lightweight)
│   │   └── ReasoningStreamingChatModelConfig.java # Heavy AI model for complex reasoning
│   ├── ratelimiter/                              # Redisson-based rate limiting
│   ├── monitor/                                  # AI model metrics → Prometheus
│   ├── annotation/ + aop/                        # @AuthCheck annotation + interceptor
│   ├── exception/                                # GlobalExceptionHandler, ErrorCode, BusinessException
│   ├── manager/CosManager.java                   # Tencent COS uploads
│   └── utils/                                    # SpringContextUtil, CacheKeyUtils, WebScreenshotUtils
├── src/main/resources/
│   ├── application.yml                           # Main config (DB, Redis, server, monitoring)
│   ├── application-local.yml                     # Local secrets (AI keys, COS credentials) — gitignored
│   ├── prompt/                                   # AI system prompt templates
│   └── mapper/                                   # XML mapper files
├── sql/create_table.sql                          # DB schema
└── yu-code-mother-frontend/                      # Vue 3 frontend
    └── src/
        ├── pages/                                # HomePage, User(Login/Register), App(Chat/Edit), Admin
        ├── components/                           # Shared components
        ├── api/                                  # Auto-generated API stubs (typed)
        ├── stores/loginUser.ts                   # Login user store (Pinia)
        ├── router/index.ts                       # Routes with admin guard in beforeEach
        ├── access.ts                             # Global auth guard
        └── config/env.ts                         # API_BASE_URL, deploy domain, etc.
```

## Key Design Patterns

- **Factory Pattern** for AI services — each app can have its own AI service instance, managed by `AiCodeGeneratorServiceFactory`
- **Facade Pattern** — `AiCodeGeneratorFacade` orchestrates LLM generation → code parsing → file saving
- **Strategy Pattern** — parsers and savers are selected by `CodeGenTypeEnum` via executor classes
- **LangGraph4j StateGraph** — workflow orchestration with conditional edges, concurrent fan-out, and quality-check loops (rerun code gen on failure)
- **Prototype-scoped beans** for AI chat models — each gets its own model instance via `@Scope("prototype")`
- **SSE streaming** via Project Reactor `Flux<ServerSentEvent<String>>` for real-time AI response delivery

## Key Flows

### AI Code Generation
1. User creates app → AI routes `initPrompt` to determine `CodeGenTypeEnum` (html / multi_file / vue_project)
2. User chats → `AppController.chatGenCode()` → SSE endpoint → `AppServiceImpl.chatToGenCode()`
3. `AiCodeGeneratorFacade` generates code via LangChain4j streaming → parser extracts code blocks → saver writes to `tmp/code_output/`
4. Vue projects also run `npm install && npm build` via `VueProjectBuilder`
5. Code can be deployed (ZIP download or static server deployment)

### LangGraph4j Workflow
1. Image collection → prompt enhancement → router → code generator → quality check → (fail → redo) | (pass → build → end)
2. Concurrent variant collects logos/diagrams/illustrations in parallel

## Important Configuration

- Backend port: `8123`, context path: `/api`
- Session: Redis-backed, 30-day timeout
- Code output: `tmp/code_output/`, deployment: `tmp/code_deploy/`
- Frontend dev server proxies `/api` → `http://localhost:8123`
- DB schema: `sql/create_table.sql`
- AI models configured in `application-local.yml` (three tiers: streaming, reasoning, routing)
- Profiling: `spring.profiles.active: local` activates `application-local.yml`
