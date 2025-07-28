# Spring Cloud RBAC 多租户管理系统

## 项目简介

这是一个基于Spring Cloud微服务架构的多租户RBAC（基于角色的访问控制）管理系统。系统采用微服务设计模式，支持多租户隔离，提供完整的用户认证、权限管理、租户管理等功能。

## 系统架构

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Gateway       │    │   Eureka        │    │   Auth Service  │
│   (网关服务)     │    │   (注册中心)     │    │   (认证服务)     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
         ┌───────────────────────┼───────────────────────┐
         │                       │                       │
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  User Service   │    │ Permission      │    │  Tenant Service │
│   (用户服务)     │    │ Service         │    │   (租户服务)     │
│                 │    │ (权限服务)       │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
                    ┌─────────────────┐
                    │  File Service   │
                    │   (文件服务)     │
                    └─────────────────┘
```

## 技术栈

### 后端技术
- **Spring Boot 3.4.0** - 应用框架
- **Spring Cloud 2024.0.2** - 微服务框架
- **Spring Security 6.4.1** - 安全框架
- **Spring Data JPA** - 数据访问层
- **PostgreSQL 15** - 主数据库
- **Redis 7** - 缓存数据库
- **Eureka Server** - 服务注册与发现
- **Spring Cloud Gateway** - API网关
- **OpenFeign** - 服务间调用
- **JWT** - 身份认证
- **Lombok** - 代码简化
- **Maven** - 项目管理

### 开发工具
- **Java 17** - 开发语言
- **IntelliJ IDEA** - IDE
- **Docker** - 容器化部署
- **Postman** - API测试

## 环境要求

- JDK 17+
- Maven 3.6+
- Docker & Docker Compose
- PostgreSQL 15+
- Redis 7+

## 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/kcodinkz/spring-cloud-rbac-demo.git
cd spring-cloud-rbac-demo
```

### 2. 启动数据库服务

#### 使用Docker启动PostgreSQL和Redis

```bash
# 创建docker-compose.yml文件
cat > docker-compose.yml << 'EOF'
version: '3.8'

services:
  postgres:
    image: postgres:15
    container_name: rbac-postgres
    environment:
      POSTGRES_DB: rbac_system
      POSTGRES_USER: rbac_user
      POSTGRES_PASSWORD: rbac123456
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - rbac-network

  redis:
    image: redis:7-alpine
    container_name: rbac-redis
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    networks:
      - rbac-network

volumes:
  postgres_data:
  redis_data:

networks:
  rbac-network:
    driver: bridge
EOF

# 启动服务
docker-compose up -d

# 检查服务状态
docker-compose ps
```

#### 手动启动数据库（可选）

```bash
# PostgreSQL
docker run -d --name rbac-postgres -e POSTGRES_DB=rbac_system  -e POSTGRES_USER=rbac_user -e POSTGRES_PASSWORD=rbac123456 -p 5432:5432 postgres:15

# Redis
docker run -d --name rbac-redis -p 6379:6379 redis:7-alpine
```

### 3. 初始化数据库

```bash
# 连接到PostgreSQL容器
docker exec -it rbac-postgres psql -U rbac_user -d rbac_system

# 或者使用psql客户端
psql -h localhost -p 5432 -U rbac_user -d rbac_system
```

### 4. 编译项目

```bash
# 编译所有模块
mvn clean compile

# 或者编译单个服务
cd auth-service && mvn clean compile
cd ../user-service && mvn clean compile
# ... 其他服务
```

### 5. 启动服务

按以下顺序启动服务：

```bash
# 1. 启动Eureka注册中心
cd eureka-server
mvn spring-boot:run

# 2. 启动认证服务
cd ../auth-service
mvn spring-boot:run

# 3. 启动用户服务
cd ../user-service
mvn spring-boot:run

# 4. 启动权限服务
cd ../permission-service
mvn spring-boot:run

# 5. 启动租户服务
cd ../tenant-service
mvn spring-boot:run

# 6. 启动文件服务
cd ../file-service
mvn spring-boot:run

# 7. 启动网关服务
cd ../gateway-service
mvn spring-boot:run
```

### 6. 验证服务

访问以下地址验证服务是否正常启动：

- **Eureka注册中心**: http://localhost:8761
- **API网关**: http://localhost:8080
- **认证服务**: http://localhost:8081
- **用户服务**: http://localhost:8082
- **权限服务**: http://localhost:8083
- **租户服务**: http://localhost:8084
- **文件服务**: http://localhost:8085

## 服务端口配置

| 服务名称 | 端口 | 说明 |
|---------|------|------|
| Eureka Server | 8761 | 服务注册中心 |
| Gateway Service | 8080 | API网关 |
| Auth Service | 8081 | 认证服务 |
| User Service | 8082 | 用户服务 |
| Permission Service | 8083 | 权限服务 |
| Tenant Service | 8084 | 租户服务 |
| File Service | 8085 | 文件服务 |

## API文档

启动服务后，可以通过以下地址访问Swagger API文档：

- **认证服务API**: http://localhost:8081/swagger-ui.html
- **用户服务API**: http://localhost:8082/swagger-ui.html
- **权限服务API**: http://localhost:8083/swagger-ui.html
- **租户服务API**: http://localhost:8084/swagger-ui.html
- **文件服务API**: http://localhost:8085/swagger-ui.html

## 项目结构

```
spring-cloud-rbac-demo/
├── eureka-server/          # 服务注册中心
├── gateway-service/         # API网关
├── auth-service/           # 认证服务
├── user-service/           # 用户服务
├── permission-service/     # 权限服务
├── tenant-service/         # 租户服务
├── file-service/           # 文件服务
├── common-module/          # 公共模块
├── database-init.sql       # 数据库初始化脚本
├── docker-compose.yml      # Docker编排文件
└── README.md              # 项目说明文档
```

## 核心功能

### 认证授权
- JWT Token认证
- 基于角色的权限控制(RBAC)
- 多租户隔离
- 会话管理

### 用户管理
- 用户注册、登录、注销
- 用户信息管理
- 密码重置
- 用户状态管理

### 权限管理
- 角色管理
- 权限分配
- 权限验证
- 动态权限控制

### 租户管理
- 租户创建、配置
- 租户隔离
- 租户资源管理
- 多租户数据隔离

### 文件管理
- 文件上传、下载
- 文件预览
- 缩略图生成
- 文件分类管理

## 开发指南

### 添加新服务

1. 在根目录创建新的服务模块
2. 在父pom.xml中添加模块
3. 配置application.yml
4. 实现业务逻辑
5. 注册到Eureka

### 数据库迁移

```bash
# 执行数据库初始化脚本
psql -h localhost -p 5432 -U rbac_user -d rbac_system -f database-init.sql
```

### 日志配置

各服务的日志配置在`application.yml`中，支持以下级别：
- DEBUG: 开发调试
- INFO: 一般信息
- WARN: 警告信息
- ERROR: 错误信息

## 部署说明

### 开发环境
- 使用Docker Compose启动数据库
- 本地运行各微服务
- 使用IDE进行调试

### 生产环境
- 使用Docker容器化部署
- 配置负载均衡
- 设置监控和日志收集
- 配置数据库集群

## 常见问题

### 1. 数据库连接失败
- 检查PostgreSQL服务是否启动
- 验证数据库连接配置
- 确认数据库用户权限

### 2. 服务注册失败
- 检查Eureka服务是否启动
- 验证网络连接
- 检查服务配置

### 3. 权限验证失败
- 检查JWT Token是否有效
- 验证用户权限配置
- 确认租户隔离设置

## 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 联系方式

- 项目维护者: [kcodinkz](https://github.com/kcodinkz)
- 项目地址: https://github.com/kcodinkz/spring-cloud-rbac-demo
- 问题反馈: [Issues](https://github.com/kcodinkz/spring-cloud-rbac-demo/issues) 