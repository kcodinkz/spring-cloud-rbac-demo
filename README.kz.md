# Spring Cloud RBAC Көп жалдаушыға арналған басқару жүйесі

## Жобаға қысқаша шолу

Бұл Spring Cloud микросервис архитектурасына негізделген көп жалдаушыға арналған RBAC (рөлге негізделген қолжетімділікті басқару) басқару жүйесі. Жүйе микросервис дизайнын қолданады, көп жалдаушы оқшаулауын қолдайды және толық пайдаланушы аутентификациясы, рұқсаттарды басқару, жалдаушыларды басқару және басқа да функцияларды ұсынады.

## Жүйе архитектурасы

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Gateway       │    │   Eureka        │    │   Auth Service  │
│   (Шлюз)        │    │   (Тіркеу)      │    │   (Аутентификация)│
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
         ┌───────────────────────┼───────────────────────┐
         │                       │                       │
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  User Service   │    │ Permission      │    │  Tenant Service │
│   (Пайдаланушы) │    │ Service         │    │   (Жалдаушы)    │
│                 │    │ (Рұқсат)        │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
                    ┌─────────────────┐
                    │  File Service   │
                    │   (Файл)        │
                    └─────────────────┘
```

## Технологиялық стек

### Артқы жақ технологиялары
- **Spring Boot 3.4.0** - Қолданба фреймворкі
- **Spring Cloud 2024.0.2** - Микросервис фреймворкі
- **Spring Security 6.4.1** - Қауіпсіздік фреймворкі
- **Spring Data JPA** - Деректерге қолжетімділік қабаты
- **PostgreSQL 15** - Негізгі дерекқор
- **Redis 7** - Кэш дерекқоры
- **Eureka Server** - Қызметтерді тіркеу және табу
- **Spring Cloud Gateway** - API шлюзі
- **OpenFeign** - Қызметтер арасындағы қоңыраулар
- **JWT** - Аутентификация
- **Lombok** - Кодты оңайлату
- **Maven** - Жобаны басқару

### Даму құралдары
- **Java 17** - Бағдарламалау тілі
- **IntelliJ IDEA** - IDE
- **Docker** - Контейнерлеу
- **Postman** - API тестілеу

## Орта талаптары

- JDK 17+
- Maven 3.6+
- Docker & Docker Compose
- PostgreSQL 15+
- Redis 7+

## Жылдам бастау

### 1. Жобаны клондау

```bash
git clone https://github.com/kcodinkz/spring-cloud-rbac-demo.git
cd spring-cloud-rbac-demo
```

### 2. Дерекқор қызметтерін іске қосу

#### Docker арқылы PostgreSQL және Redis іске қосу

```bash
# docker-compose.yml жасаңыз
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

# Қызметтерді іске қосу
docker-compose up -d

# Қызмет күйін тексеру
docker-compose ps
```

#### Дерекқорларды қолмен іске қосу (міндетті емес)

```bash
# PostgreSQL
docker run -d --name rbac-postgres -e POSTGRES_DB=rbac_system  -e POSTGRES_USER=rbac_user -e POSTGRES_PASSWORD=rbac123456 -p 5432:5432 postgres:15

# Redis
docker run -d --name rbac-redis -p 6379:6379 redis:7-alpine
```

### 3. Дерекқорды инициализациялау

```bash
# PostgreSQL контейнеріне қосылу
docker exec -it rbac-postgres psql -U rbac_user -d rbac_system

# Немесе psql клиентін пайдалану
psql -h localhost -p 5432 -U rbac_user -d rbac_system
```

### 4. Жобаны құрастыру

```bash
# Барлық модульдерді құрастыру
mvn clean compile

# Немесе жеке қызметті құрастыру
cd auth-service && mvn clean compile
cd ../user-service && mvn clean compile
# ... басқа қызметтер
```

### 5. Қызметтерді іске қосу

Қызметтерді келесі ретпен іске қосыңыз:

```bash
# 1. Eureka тіркеу орталығын іске қосу
cd eureka-server
mvn spring-boot:run

# 2. Аутентификация қызметін іске қосу
cd ../auth-service
mvn spring-boot:run

# 3. Пайдаланушы қызметін іске қосу
cd ../user-service
mvn spring-boot:run

# 4. Рұқсат қызметін іске қосу
cd ../permission-service
mvn spring-boot:run

# 5. Жалдаушы қызметін іске қосу
cd ../tenant-service
mvn spring-boot:run

# 6. Файл қызметін іске қосу
cd ../file-service
mvn spring-boot:run

# 7. Шлюз қызметін іске қосу
cd ../gateway-service
mvn spring-boot:run
```

### 6. Қызметтерді тексеру

Келесі мекенжайларға өтіп, қызметтердің жұмысын тексеріңіз:

- **Eureka тіркеу орталығы**: http://localhost:8761
- **API шлюзі**: http://localhost:8080
- **Аутентификация қызметі**: http://localhost:8081
- **Пайдаланушы қызметі**: http://localhost:8082
- **Рұқсат қызметі**: http://localhost:8083
- **Жалдаушы қызметі**: http://localhost:8084
- **Файл қызметі**: http://localhost:8085

## Қызмет порттарының конфигурациясы

| Қызмет атауы         | Порт | Сипаттама              |
|---------------------|------|------------------------|
| Eureka Server       | 8761 | Қызметтерді тіркеу     |
| Gateway Service     | 8080 | API шлюзі              |
| Auth Service        | 8081 | Аутентификация         |
| User Service        | 8082 | Пайдаланушы басқару    |
| Permission Service  | 8083 | Рұқсат қызметі         |
| Tenant Service      | 8084 | Жалдаушы басқару       |
| File Service        | 8085 | Файл басқару           |

## API құжаттамасы

Қызметтер іске қосылғаннан кейін Swagger API құжаттамасына келесі мекенжайлар арқылы қол жеткізуге болады:

- **Аутентификация қызметі API**: http://localhost:8081/swagger-ui.html
- **Пайдаланушы қызметі API**: http://localhost:8082/swagger-ui.html
- **Рұқсат қызметі API**: http://localhost:8083/swagger-ui.html
- **Жалдаушы қызметі API**: http://localhost:8084/swagger-ui.html
- **Файл қызметі API**: http://localhost:8085/swagger-ui.html

## Жоба құрылымы

```
spring-cloud-rbac-demo/
├── eureka-server/          # Қызметтерді тіркеу орталығы
├── gateway-service/        # API шлюзі
├── auth-service/           # Аутентификация қызметі
├── user-service/           # Пайдаланушы қызметі
├── permission-service/     # Рұқсат қызметі
├── tenant-service/         # Жалдаушы қызметі
├── file-service/           # Файл қызметі
├── common-module/          # Жалпы модуль
├── database-init.sql       # Дерекқорды инициализациялау скрипті
├── docker-compose.yml      # Docker compose файлы
└── README.md               # Жоба құжаттамасы
```

## Негізгі функциялар

### Аутентификация және рұқсат
- JWT Token аутентификациясы
- Рөлге негізделген қолжетімділікті басқару (RBAC)
- Көп жалдаушы оқшаулау
- Сессияны басқару

### Пайдаланушыны басқару
- Пайдаланушыны тіркеу, кіру, шығу
- Пайдаланушы профилін басқару
- Құпиясөзді қалпына келтіру
- Пайдаланушы күйін басқару

### Рұқсаттарды басқару
- Рөлдерді басқару
- Рұқсаттарды тағайындау
- Рұқсаттарды тексеру
- Динамикалық рұқсаттарды басқару

### Жалдаушыларды басқару
- Жалдаушыны құру және баптау
- Жалдаушы оқшаулау
- Жалдаушы ресурстарын басқару
- Көп жалдаушы деректерін оқшаулау

### Файлдарды басқару
- Файлдарды жүктеу және жүктеп алу
- Файлдарды алдын ала қарау
- Миниатюра жасау
- Файлдарды санаттау

## Даму бойынша нұсқаулық

### Жаңа қызмет қосу

1. Түбір каталогта жаңа қызмет модулін жасаңыз
2. Модульді ата-ана pom.xml-ге қосыңыз
3. application.yml конфигурациялаңыз
4. Бизнес логиканы жүзеге асырыңыз
5. Eureka-ға тіркеңіз

### Дерекқорды көшіру

```bash
# Дерекқорды инициализациялау скриптін іске қосу
psql -h localhost -p 5432 -U rbac_user -d rbac_system -f database-init.sql
```

### Журналды баптау

Әр қызметтің журналын `application.yml` арқылы баптауға болады және келесі деңгейлерді қолдайды:
- DEBUG: Даму/дебаг
- INFO: Жалпы ақпарат
- WARN: Ескерту
- ERROR: Қате

## Жайғастыру бойынша нұсқаулық

### Даму ортасы
- Docker Compose арқылы дерекқорды іске қосу
- Микросервистерді локалды іске қосу
- IDE арқылы дебаг жасау

### Өндірістік орта
- Docker контейнерлерімен орналастыру
- Жүктемені теңестіруді баптау
- Мониторинг және журнал жинауды орнату
- Дерекқор кластерін баптау

## Жиі қойылатын сұрақтар

### 1. Дерекқорға қосылу сәтсіз аяқталды
- PostgreSQL іске қосылғанын тексеріңіз
- Дерекқор қосылымын тексеріңіз
- Дерекқор пайдаланушысының рұқсаттарын растаңыз

### 2. Қызметті тіркеу сәтсіз аяқталды
- Eureka іске қосылғанын тексеріңіз
- Желі байланысын тексеріңіз
- Қызмет конфигурациясын тексеріңіз

### 3. Рұқсатты тексеру сәтсіз аяқталды
- JWT Token жарамды екенін тексеріңіз
- Пайдаланушы рұқсаттарын тексеріңіз
- Жалдаушы оқшаулауын растаңыз

## Үлес қосу нұсқаулығы

1. Жобаны Fork етіңіз
2. Функция тармағын жасаңыз (`git checkout -b feature/AmazingFeature`)
3. Өзгерістерді жіберіңіз (`git commit -m 'Add some AmazingFeature'`)
4. Тармаққа итеріңіз (`git push origin feature/AmazingFeature`)
5. Pull Request ашыңыз

## Лицензия

Бұл жоба MIT лицензиясымен лицензияланған - толық ақпаратты [LICENSE](LICENSE) файлын қараңыз

## Байланыс

- Жобаны қолдаушы: [kcodinkz](https://github.com/kcodinkz)
- Жоба сілтемесі: https://github.com/kcodinkz/spring-cloud-rbac-demo
- Мәселелер: [Issues](https://github.com/kcodinkz/spring-cloud-rbac-demo/issues) 