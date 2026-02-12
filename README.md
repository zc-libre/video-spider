# Video Spider

多源视频爬虫与管理平台，支持多个视频网站的自动化爬取、M3U8 流媒体下载、FFmpeg 转码、全文搜索和在线播放。仅供学习，切勿传播商用。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Java 21 + Spring Boot 3.2.4 |
| 并发模型 | Java 21 虚拟线程 (Virtual Threads) |
| 批处理 | Spring Batch |
| 数据库 | PostgreSQL 15 (按月分区表) |
| 数据库迁移 | Liquibase |
| ORM | MyBatis Plus 3.0.3 |
| 全文搜索 | Elasticsearch + IK 中文分词 |
| 缓存/会话 | Redis 7.0 |
| 视频处理 | FFmpeg (M3U8 下载 + MP4 转码) |
| 前端 | React 19 + TypeScript + Vite |
| UI | Radix UI + Tailwind CSS (暗色主题) |
| 视频播放 | HLS.js |
| 容器化 | Docker Compose |

## 项目结构

```
video-spider/
├── src/main/java/com/libre/video/
│   ├── VideoSpiderApplication.java    # 应用入口
│   ├── controller/                    # REST API
│   │   ├── VideoController            # 视频操作 (爬虫/搜索/下载/播放)
│   │   └── UserController             # 用户认证 (登录/登出)
│   ├── service/                       # 业务逻辑
│   │   └── impl/VideoServiceImpl      # 核心服务实现
│   ├── core/
│   │   ├── spider/                    # 爬虫模块
│   │   │   ├── reader/                # 4 个数据源读取器
│   │   │   ├── processor/             # 4 个页面解析处理器
│   │   │   ├── writer/                # 数据写入器
│   │   │   └── VideoSpiderJobBuilder  # Spring Batch Job 构建
│   │   ├── download/                  # 视频下载与转码
│   │   │   ├── M3u8Download           # M3U8 清单下载与解析
│   │   │   └── VideoEncoder           # FFmpeg 编码 (仅手动下载时使用)
│   │   ├── sync/                      # Elasticsearch 数据同步
│   │   ├── task/                      # 定时任务 (每日爬取/数据同步)
│   │   └── event/                     # 事件驱动 (保存/上传事件)
│   ├── config/                        # 配置 (线程池/Batch/认证拦截器)
│   ├── mapper/                        # 数据访问 (MyBatis + ES Repository)
│   ├── pojo/                          # 实体与 DTO
│   └── toolkit/                       # 工具类
├── src/main/resources/
│   ├── application.yml                # 应用配置 (dev/prod 双环境)
│   └── db/changelog/                  # Liquibase 迁移脚本
├── frontend/                          # React 前端
│   └── src/
│       ├── pages/                     # 页面 (首页/登录)
│       ├── components/                # 组件 (视频网格/播放器/管理面板)
│       ├── api/                       # HTTP 接口层
│       └── hooks/                     # React Hooks (认证/视频)
├── docker/                            # Docker 构建文件 (ES + IK 分词)
├── docker-compose.yml                 # 容器编排
├── Dockerfile                         # 后端多阶段构建
└── start.sh                           # Systemd 部署脚本
```

## 核心流程

### 爬虫数据流

```
触发 (API 手动 / 定时任务每日 3:00)
  │
  ▼
Reader ── 访问源站，分页抓取视频列表页
  │
  ▼
Processor ── 解析 HTML/DOM，提取标题/封面/时长/播放链接
  │
  ▼
Writer ── 通过事件发布，触发异步保存
  │
  ▼
VideoEventListener ── 批量写入 PostgreSQL + 下载 M3U8 清单内容
  │
  ▼
前端 HLS.js ── 直接播放 M3U8 流媒体 (无需本地转码)
```

爬虫基于 **Spring Batch** 实现 (Reader → Processor → Writer)，每个数据源各有独立的 Reader 和 Processor，通过 `VideoSpiderJobBuilder` 动态构建 Job。支持配置最大爬取页数，每页间隔 3 秒防反爬。

爬虫主流程仅保存 M3U8 清单内容到数据库，前端通过 HLS.js 直接解析播放，不做本地 TS 下载和 FFmpeg 转码。项目中的 `VideoEncoder` (FFmpeg 转码) 仅在手动调用下载接口时使用。

### 搜索流程

```
用户输入关键词
  │
  ├─→ /suggest ── Elasticsearch Completion Suggester 返回搜索建议
  │
  └─→ /list ── Elasticsearch 全文检索 (IK 中文分词) + 分页返回结果
```

### 数据同步

```
PostgreSQL ──(MyBatisPagingItemReader, 每批 2000 条)──→ Elasticsearch
```

通过 Spring Batch Job 实现增量同步，可手动触发或定时执行。

## 支持的数据源

| 编号 | 类型 | 枚举值 |
|------|------|--------|
| 1 | 91porn | `REQUEST_91` |
| 2 | 91porny | `REQUEST_9S` |
| 3 | tasexy | `REQUEST_BA_AV` |
| 4 | heiliao | `REQUEST_HEILIAO` |

## API 接口

### 视频接口 `/api/video`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/spider/{type}?maxPages=100` | 触发指定源的爬虫任务 |
| POST | `/list` | 分页搜索视频列表 |
| GET | `/suggest?q=&size=10` | 搜索关键词补全 |
| GET | `/watch/{videoId}` | 获取视频播放地址 |
| GET | `/download/{id}` | 下载视频 (FFmpeg 转码) |
| POST | `/download?videoUrl=` | 通过 URL 下载视频 |
| GET | `/sync` | 手动触发 ES 数据同步 |

### 用户接口 `/video/user`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/login` | 登录 (返回 Token, 存 Redis, 24h 过期) |
| POST | `/info` | 获取用户信息 (需 Authorization Header) |
| POST | `/logout` | 登出 |

## 快速开始

### 环境要求

- Java 21+
- Maven 3.9+
- Node.js 22+
- Docker & Docker Compose
- FFmpeg (本地开发需要)

### Docker Compose 部署 (推荐)

1. 配置环境变量

```bash
cp .env.example .env
# 编辑 .env 文件，设置数据库密码等
```

2. 启动全部服务

```bash
docker-compose up -d
```

3. 访问服务

| 服务 | 地址 |
|------|------|
| 前端 | http://localhost:15302 |
| 后端 API | http://localhost:15301 |
| PostgreSQL | localhost:2345 |
| Elasticsearch | localhost:9201 |
| Redis | localhost:15303 |

### 本地开发

1. 启动基础设施 (PostgreSQL / Redis / Elasticsearch)

2. 启动后端

```bash
mvn clean package -DskipTests
java -jar target/video-spider-1.0.0.jar --spring.profiles.active=dev
```

3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

## Docker 服务编排

```
┌─────────────┐     ┌──────────────┐
│  Frontend   │────▶│  Backend     │
│  (Nginx)    │     │  (Spring)    │
│  :15302     │     │  :15301      │
└─────────────┘     └──────┬───────┘
                           │
              ┌────────────┼────────────┐
              ▼            ▼            ▼
      ┌────────────┐ ┌──────────┐ ┌─────────┐
      │ PostgreSQL │ │  Redis   │ │   ES    │
      │  :2345     │ │  :15303  │ │  :9201  │
      └────────────┘ └──────────┘ └─────────┘
```

## 架构特点

- **Spring Batch 爬虫框架**: Reader/Processor/Writer 三阶段流水线，支持容错自动跳过异常记录
- **Java 21 虚拟线程**: 轻量级并发模型，替代传统线程池，提升爬虫吞吐量
- **事件驱动**: 通过 Spring Event 解耦视频保存与异步转码流程
- **PostgreSQL 按月分区**: 海量数据按时间分片存储，查询时自动分区裁剪
- **IK 中文分词**: Elasticsearch 集成 IK 分词器，支持中文视频标题的精确搜索和补全建议
- **多阶段 Docker 构建**: 编译产物与运行环境分离，减小镜像体积

## 许可证

[LICENSE](LICENSE)
