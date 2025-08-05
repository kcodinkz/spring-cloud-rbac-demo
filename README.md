
多语言入口 | Language: [English](README.md) | [简体中文](README.zh.md) | [Қазақша](README.kz.md)


# Spring Cloud RBAC Multi-Tenant Management System

## Project Overview

This is a multi-tenant RBAC (Role-Based Access Control) management system based on the Spring Cloud microservices architecture. The system adopts a microservices design pattern, supports multi-tenant isolation, and provides complete user authentication, permission management, tenant management, and other features.

## System Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Gateway       │    │   Eureka        │    │   Auth Service  │
│   (Gateway)     │    │   (Registry)    │    │   (Auth)        │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
         ┌───────────────────────┼───────────────────────┐
         │                       │                       │
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  User Service   │    │ Permission      │    │  Tenant Service │
│   (User)        │    │ Service         │    │   (Tenant)      │
│                 │    │ (Permission)    │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
                    ┌─────────────────┐
                    │  File Service   │
                    │   (File)        │
                    └─────────────────┘
```

## Technology Stack

### Backend Technologies
- **Spring Boot 3.4.0** - Application framework
- **Spring Cloud 2024.0.2** - Microservices framework
- **Spring Security 6.4.1** - Security framework
- **Spring Data JPA** - Data access layer
- **PostgreSQL 15** - Main database
- **Redis 7** - Cache database
- **Eureka Server** - Service registry and discovery
- **Spring Cloud Gateway** - API gateway
- **OpenFeign** - Service-to-service calls
- **JWT** - Authentication
- **Lombok** - Code simplification
- **Maven** - Project management

### Development Tools
- **Java 17** - Programming language
- **IntelliJ IDEA** - IDE
- **Docker** - Containerization
- **Postman** - API testing

## Environment Requirements

- JDK 17+
- Maven 3.6+
- Docker & Docker Compose
- PostgreSQL 15+
- Redis 7+

## Quick Start

### 1. Clone the Project

```bash
git clone https://github.com/kcodinkz/spring-cloud-rbac-demo.git
cd spring-cloud-rbac-demo
```

### 2. Start Database Services

#### Start PostgreSQL and Redis with Docker

```bash
# Create docker-compose.yml
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

# Start services
docker-compose up -d

# Check service status
docker-compose ps
```

#### Manually Start Databases (Optional)

```bash
# PostgreSQL
docker run -d --name rbac-postgres -e POSTGRES_DB=rbac_system  -e POSTGRES_USER=rbac_user -e POSTGRES_PASSWORD=rbac123456 -p 5432:5432 postgres:15

# Redis
docker run -d --name rbac-redis -p 6379:6379 redis:7-alpine
```

### 3. Initialize the Database

```bash
# Connect to PostgreSQL container
docker exec -it rbac-postgres psql -U rbac_user -d rbac_system

# Or use psql client
psql -h localhost -p 5432 -U rbac_user -d rbac_system
```

### 4. Build the Project

```bash
# Build all modules
mvn clean compile

# Or build a single service
cd auth-service && mvn clean compile
cd ../user-service && mvn clean compile
# ... other services
```

### 5. Start Services

Start the services in the following order:

```bash
# 1. Start Eureka registry
cd eureka-server
mvn spring-boot:run

# 2. Start Auth Service
cd ../auth-service
mvn spring-boot:run

# 3. Start User Service
cd ../user-service
mvn spring-boot:run

# 4. Start Permission Service
cd ../permission-service
mvn spring-boot:run

# 5. Start Tenant Service
cd ../tenant-service
mvn spring-boot:run

# 6. Start File Service
cd ../file-service
mvn spring-boot:run

# 7. Start Gateway Service
cd ../gateway-service
mvn spring-boot:run
```

### 6. Verify Services

Visit the following addresses to verify services are running:

- **Eureka Registry**: http://localhost:8761
- **API Gateway**: http://localhost:8080
- **Auth Service**: http://localhost:8081
- **User Service**: http://localhost:8082
- **Permission Service**: http://localhost:8083
- **Tenant Service**: http://localhost:8084
- **File Service**: http://localhost:8085

## Service Port Configuration

| Service Name        | Port | Description         |
|--------------------|------|--------------------|
| Eureka Server      | 8761 | Service registry   |
| Gateway Service    | 8080 | API gateway        |
| Auth Service       | 8081 | Authentication     |
| User Service       | 8082 | User management    |
| Permission Service | 8083 | Permission service |
| Tenant Service     | 8084 | Tenant management  |
| File Service       | 8085 | File management    |

## API Documentation

After starting the services, access Swagger API docs at:

- **Auth Service API**: http://localhost:8081/swagger-ui.html
- **User Service API**: http://localhost:8082/swagger-ui.html
- **Permission Service API**: http://localhost:8083/swagger-ui.html
- **Tenant Service API**: http://localhost:8084/swagger-ui.html
- **File Service API**: http://localhost:8085/swagger-ui.html

## Project Structure

```
spring-cloud-rbac-demo/
├── eureka-server/          # Service registry
├── gateway-service/        # API gateway
├── auth-service/           # Authentication service
├── user-service/           # User service
├── permission-service/     # Permission service
├── tenant-service/         # Tenant service
├── file-service/           # File service
├── common-module/          # Common module
├── database-init.sql       # Database init script
├── docker-compose.yml      # Docker compose file
└── README.md               # Project documentation
```

## Core Features

### Authentication & Authorization
- JWT Token authentication
- Role-based access control (RBAC)
- Multi-tenant isolation
- Session management

### User Management
- User registration, login, logout
- User profile management
- Password reset
- User status management

### Permission Management
- Role management
- Permission assignment
- Permission validation
- Dynamic permission control

### Tenant Management
- Tenant creation and configuration
- Tenant isolation
- Tenant resource management
- Multi-tenant data isolation

### File Management
- File upload and download
- File preview
- Thumbnail generation
- File categorization

## Development Guide

### Add a New Service

1. Create a new service module in the root directory
2. Add the module to the parent pom.xml
3. Configure application.yml
4. Implement business logic
5. Register with Eureka

### Database Migration

```bash
# Run database initialization script
psql -h localhost -p 5432 -U rbac_user -d rbac_system -f database-init.sql
```

### Logging Configuration

Each service's logging is configured in `application.yml` and supports the following levels:
- DEBUG: Development/debug
- INFO: General info
- WARN: Warning
- ERROR: Error

## Deployment Guide

### Development Environment
- Use Docker Compose to start the database
- Run microservices locally
- Debug with IDE

### Production Environment
- Deploy with Docker containers
- Configure load balancing
- Set up monitoring and log collection
- Configure database cluster

## FAQ

### 1. Database connection failed
- Check if PostgreSQL is running
- Verify database connection config
- Confirm database user permissions

### 2. Service registration failed
- Check if Eureka is running
- Verify network connectivity
- Check service configuration

### 3. Permission validation failed
- Check if JWT Token is valid
- Verify user permission config
- Confirm tenant isolation settings

## Contribution Guide

1. Fork the project
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details

## Contact

- Maintainer: [kcodinkz](https://github.com/kcodinkz)
- Project URL: https://github.com/kcodinkz/spring-cloud-rbac-demo
- Issue Tracker: [Issues](https://github.com/kcodinkz/spring-cloud-rbac-demo/issues) 