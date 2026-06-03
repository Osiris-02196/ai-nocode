
<div align="center">
  <h1>🤖 零代码应用生成平台</h1>
  <p><strong>零代码应用生成平台 — 用自然语言描述需求，AI 即刻生成完整应用</strong></p>

  <p>
    <img src="https://img.shields.io/badge/Java-21-orange?logo=java" alt="Java 21"/>
    <img src="https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen?logo=springboot" alt="Spring Boot 3.5"/>
    <img src="https://img.shields.io/badge/Vue-3.5-%234FC08D?logo=vuedotjs" alt="Vue 3.5"/>
    <img src="https://img.shields.io/badge/langchain4j-1.12-blue" alt="LangChain4j"/>
    <img src="https://img.shields.io/badge/langgraph4j-1.6-purple" alt="LangGraph4j"/>
    <img src="https://img.shields.io/badge/license-MIT-green" alt="License"/>
  </p>
</div>

---

## 📖 项目简介

**ai-nocode** 是一个基于 AI 的零代码应用生成平台。用户只需用自然语言描述想要的应用，AI 就能自动生成可直接运行的 Web 应用——支持纯 HTML 页面、多文件项目，以及完整的 Vue 3 工程。

项目结合了 **LangChain4j** 的 AI 对话能力和 **LangGraph4j** 的工作流编排能力，构建了一套从需求理解→代码生成→质量检查→自动部署的完整流水线。

## ✨ 核心特性

- 🗣️ **自然语言生成** — 用大白话描述需求，AI 理解并生成代码
- 📄 **三种生成模式** — 单 HTML 文件 / 多文件项目 / Vue 3 完整工程
- ⚡ **流式对话** — SSE 实时推送 AI 生成内容，类 ChatGPT 的交互体验
- 🔄 **智能工作流** — LangGraph4j 驱动：图片收集 → Prompt 增强 → 代码生成 → 质量检查 → 自动构建
- 🧩 **并行生成** — Logo、插画、Mermaid 图表并行采集，提升效率
- ✅ **质量保障** — 自动代码质量检查，不合格则触发重生成
- 🚀 **一键部署** — 生成完成后可部署到静态服务器，并自动截图预览
- 🔐 **用户体系** — 登录注册、权限控制、管理员后台
- 📊 **监控与限流** — Prometheus 指标 + Redisson 分布式限流

## 🖥️ 界面截图

### 🏠 首页

![首页](assets/screenshots/homepage.png)

---

### 📁 我的作品

![我的应用](assets/screenshots/myworks.png)

---

### 🤖 AI 对话生成

![AI对话](assets/screenshots/ai-chat.png)

---

## 🏗️ 技术架构

### 后端技术栈

| 技术 | 说明 |
|------|------|
| **Java 21** | 运行环境，使用虚拟线程 |
| **Spring Boot 3.5.12** | 基础框架 |
| **MyBatis-Flex 1.11.6** | ORM 框架，灵活高效 |
| **MySQL + HikariCP** | 数据库与连接池 |
| **Redis** | 会话管理 / Redisson 分布式锁 / Caffeine 本地缓存 |
| **LangChain4j 1.12.2** | AI 集成（流式对话、工具调用、防护栏、AI Services） |
| **LangGraph4j 1.6.0** | 工作流编排（状态图、条件边、并发节点） |
| **Project Reactor** | SSE 流式响应 |
| **DashScope SDK** | 阿里云通义万相（AI 图片生成） |
| **Selenium + WebDriver** | 网页截图 |
| **腾讯云 COS** | 对象存储（截图、资源） |
| **Knife4j** | API 文档 |
| **Prometheus + Micrometer** | AI 模型调用监控 |

### 前端技术栈

| 技术 | 说明 |
|------|------|
| **Vue 3.5** | 前端框架 |
| **TypeScript 5.8** | 类型安全 |
| **Vite 7** | 构建工具 |
| **Ant Design Vue 4** | UI 组件库 |
| **Pinia** | 状态管理 |
| **Vue Router** | 路由管理（含路由级权限控制） |
| **Axios** | HTTP 客户端 |
| **Markdown-it + Highlight.js** | AI 回复渲染 |

## 📁 项目结构

```
yu-ai-code-mother/
├── src/main/java/com/oxiris/yuaicodemother/
│   ├── YuAiCodeMotherApplication.java        # 入口
│   ├── ai/                                    # AI 代码生成服务（接口、工厂、路由、工具）
│   ├── core/                                  # 代码生成管道（Facade → Parser → Saver → Builder）
│   │   ├── AiCodeGeneratorFacade.java         # 统一门面
│   │   ├── parser/                            # HTML/多文件代码解析器
│   │   ├── saver/                             # HTML/多文件代码保存器
│   │   └── builder/VueProjectBuilder.java     # Vue 项目构建器 (npm build)
│   ├── langgraph4j/                           # LangGraph4j 工作流
│   │   ├── CodeGenWorkflow.java               # 完整串行工作流
│   │   ├── CodeGenConcurrentWorkflow.java     # 并行采集工作流
│   │   ├── node/                              # 工作流节点
│   │   └── tools/                             # 图片搜索/Mermaid/Logo/插画工具
│   ├── controller/                            # REST 控制器
│   ├── service/                               # 业务服务层
│   ├── mapper/                                # MyBatis-Flex 映射接口
│   ├── model/                                 # 实体 / DTO / VO / 枚举
│   ├── config/                                # Spring 配置（AI模型、Redis、CORS、COS）
│   ├── ratelimiter/                           # Redisson 分布式限流
│   ├── monitor/                               # AI 模型监控 → Prometheus
│   └── exception/                             # 全局异常处理
├── src/main/resources/
│   ├── application.yml                        # 主配置
│   ├── application-local.yml                  # 本地密钥配置（gitignored）
│   └── prompt/                                # AI 系统提示词
├── sql/create_table.sql                       # 数据库建表脚本
└── yu-code-mother-frontend/                   # Vue 3 前端
    └── src/
        ├── pages/                             # 页面（首页/用户/应用对话/管理后台）
        ├── components/                        # 公共组件
        ├── api/                               # API 调用层（自动生成）
        └── stores/                            # Pinia 状态
```

## 🚀 快速启动

### 前置要求

- JDK 21+
- Node.js 22+
- MySQL 8+
- Redis 7+
- Maven（可使用项目自带的 `mvnw`）

### 1. 数据库初始化

```sql
-- 创建数据库
CREATE DATABASE yu_ai_code_mother CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 导入表结构
mysql -u root -p yu_ai_code_mother < sql/create_table.sql
```

### 2. 后端启动

```bash
# 1. 配置本地密钥文件
# 复制并编辑 application-local.yml，填入 AI API Key、COS 密钥等

# 2. 启动服务（默认端口 8123）
./mvnw spring-boot:run
```

> **注意**：`application-local.yml` 包含敏感信息（AI API Key、云存储密钥等），默认已加入 `.gitignore`。需要根据 `application.yml` 中的配置项创建对应的本地配置文件。

### 3. 前端启动

```bash
cd yu-code-mother-frontend

# 安装依赖
npm install

# 启动开发服务器（默认 :5173，自动代理 /api 到 :8123）
npm run dev
```

浏览器访问 `http://localhost:5173` 即可使用。

### 其他常用命令

```bash
# 后端构建（跳过测试）
./mvnw clean package -DskipTests

# 后端测试
./mvnw test

# 前端构建
cd yu-code-mother-frontend && npm run build

# 前端代码检查
npm run lint

# 前端代码格式化
npm run format

# 根据后端 OpenAPI 生成前端类型定义
npm run openapi2ts
```

## ⚙️ 配置说明

### 后端主要配置 (`application.yml`)

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `server.port` | 8123 | 后端服务端口 |
| `server.servlet.context-path` | /api | API 路径前缀 |
| `spring.datasource.url` | jdbc:mysql://localhost:3306/yu_ai_code_mother | MySQL 连接 |
| `spring.data.redis.host` | localhost | Redis 地址 |
| `spring.session.store-type` | redis | 基于 Redis 的会话存储 |

### AI 模型配置（在 `application-local.yml` 中配置）

平台使用了三个等级的 AI 模型：

1. **Streaming Chat Model** — 主对话模型，用于代码生成对话
2. **Reasoning Chat Model** — 推理模型，用于复杂推理任务
3. **Routing Chat Model** — 轻量模型，用于简单的类型分类

## 🔄 核心工作流

### AI 代码生成流水线

```
用户输入需求
    ↓
AI 智能路由（CodeGenTypeRoutingService）
    ├─→ HTML 模式：生成单个 HTML 文件
    ├─→ MULTI_FILE 模式：生成多文件项目
    └─→ VUE_PROJECT 模式：生成完整 Vue 3 工程（含自动 npm build）
    ↓
SSE 流式返回 AI 生成内容
    ↓
实时解析代码块 → 保存到 tmp/code_output/
    ↓
一键部署到静态服务器 / 下载 ZIP 包
```

### LangGraph4j 智能工作流

```
[图片收集] → [Prompt增强] → [类型路由] → [代码生成]
                                                ↓
                                           [质量检查]
                                           ↙     ↓    ↘
                                      不合格   通过HTML  通过多文件
                                      /多文件    → 部署   → 部署
                                      ↓
                                  [重新生成代码]
```

## 📦 生成模式详解

### HTML 模式
- 生成一个独立的 `index.html` 文件
- 所有样式和脚本内联在文件中
- 适合简单页面、工具页、落地页

### Multi-File 模式
- 生成 `index.html` + `style.css` + `script.js` 等多文件
- 代码结构清晰，易于二次开发
- 适合中等复杂度的应用

### Vue Project 模式
- 生成完整的 Vue 3 + TypeScript 工程
- 包含 `package.json`、组件文件、路由配置
- 自动执行 `npm install && npm run build`
- 适合复杂 SPA 应用

## 🧪 测试

```bash
# 运行所有测试
./mvnw test

# 运行指定测试类
./mvnw test -Dtest=AiCodeGeneratorServiceTest
./mvnw test -Dtest=CodeGenWorkflowTest

# 运行单个测试方法
./mvnw test -Dtest=MermaidDiagramToolTest#testGenerateDiagram
```

## 📜 开源协议

本项目基于 MIT 协议开源。

## 🙏 致谢

- [程序员鱼皮](https://github.com/liyupi) — 项目学习与启发
- [LangChain4j](https://github.com/langchain4j/langchain4j) — Java AI 集成框架
- [LangGraph4j](https://github.com/bsorrentino/langgraph4j) — Java 工作流引擎
- [Ant Design Vue](https://github.com/vueComponent/ant-design-vue) — 优秀的 UI 组件库
