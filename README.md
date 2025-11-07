# Kanban API - Spring Boot Back-end

A fully functional Kanban board REST API built with Spring Boot, providing task management capabilities with real-time WebSocket notifications, JWT authentication, and comprehensive API documentation.

## Technology Choices

### Core Framework
- **Spring Boot 3.2.0**: Modern, production-ready framework with excellent ecosystem support
- **Java 17**: LTS version providing modern language features and performance improvements

### Persistence Layer
- **Spring Data JPA**: Simplifies database operations with repository pattern
- **Hibernate**: Industry-standard ORM for entity management
- **PostgreSQL 15**: Robust, open-source relational database with excellent performance
- **Flyway**: Database migration tool ensuring version-controlled schema changes

### Security
- **Spring Security**: Comprehensive security framework
- **JWT (JSON Web Tokens)**: Stateless authentication for scalable API access
- **BCrypt**: Secure password hashing (for future user management)

### Real-time Communication
- **Spring WebSocket**: WebSocket support for real-time bidirectional communication
- **STOMP**: Simple Text Oriented Messaging Protocol for WebSocket messaging
- **SockJS**: Fallback support for browsers that don't support WebSocket

### API Documentation
- **SpringDoc OpenAPI 3**: Automatic OpenAPI 3 specification generation
- **Swagger UI**: Interactive API documentation and testing interface

### Testing
- **JUnit 5**: Modern testing framework
- **Mockito**: Mocking framework for unit tests
- **Testcontainers**: Integration testing with real PostgreSQL containers
- **Spring Boot Test**: Comprehensive testing support

### Build & Deployment
- **Gradle**: Modern build tool with excellent dependency management
- **Docker**: Containerization for consistent deployment
- **Docker Compose**: Multi-container orchestration

### Observability
- **Spring Boot Actuator**: Production-ready monitoring and metrics
- **Prometheus**: Metrics endpoint for monitoring systems

## Features

- ✅ REST CRUD operations for tasks
- ✅ Pagination, filtering, and sorting
- ✅ JWT-based authentication
- ✅ Real-time WebSocket notifications
- ✅ OpenAPI 3 / Swagger UI documentation
- ✅ Bean validation with custom error handling
- ✅ Optimistic locking for concurrent updates
- ✅ JSON Merge Patch support for partial updates
- ✅ Database migrations with Flyway
- ✅ Comprehensive test coverage (≥80%)
- ✅ Dockerized environment
- ✅ Health checks and metrics endpoints

## Prerequisites

- Java 17 or higher
- Docker and Docker Compose (for containerized deployment)
- PostgreSQL 15 (for local development without Docker)

## Running the Application

### Option 1: Using Docker Compose (Recommended)

1. Clone the repository:
```bash
git clone <repository-url>
cd task
```

2. Build and start the application:
```bash
docker-compose up --build
```

The application will be available at:
- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- GraphiQL: http://localhost:8080/graphiql.html
- WebSocket Test: http://localhost:8080/websocket-test.html
- Health Check: http://localhost:8080/actuator/health

### Option 2: Local Development

1. Start PostgreSQL database:
```bash
docker run -d \
  --name kanban-postgres \
  -e POSTGRES_DB=kanban_db \
  -e POSTGRES_USER=kanban_user \
  -e POSTGRES_PASSWORD=kanban_pass \
  -p 5432:5432 \
  postgres:15-alpine
```

2. Build the application:
```bash
./gradlew build
```

3. Run the application:
```bash
./gradlew bootRun
```

Or run the JAR:
```bash
java -jar build/libs/kanban-api-1.0.0.jar
```

### Option 3: Using Gradle Wrapper

```bash
./gradlew bootRun
```

## Testing Interfaces

The application provides interactive HTML pages for testing WebSocket notifications and GraphQL queries:

### WebSocket Test Page

Access the WebSocket test interface at: **http://localhost:8080/websocket-test.html**

This page allows you to:
- Connect to the WebSocket endpoint (`/ws`)
- Subscribe to task notifications (`/topic/tasks`)
- Create, update, and delete tasks via REST API
- View real-time WebSocket notifications when tasks are modified
- Test the complete WebSocket notification flow

**Features:**
- Real-time connection status indicator
- JWT token management (get token button)
- Full CRUD operations for tasks
- Live message display showing all WebSocket events
- Event types: `CREATED`, `UPDATED`, `DELETED`

### GraphiQL Interface

Access the GraphQL interface at: **http://localhost:8080/graphiql.html**

This interactive GraphQL playground allows you to:
- Write and execute GraphQL queries and mutations
- Explore the GraphQL schema
- Test all GraphQL operations (queries and mutations)
- View query results in real-time

**Example Query:**
```graphql
query {
  tasks {
    content {
      id
      title
      status
      priority
    }
    totalElements
  }
}
```

**Example Mutation:**
```graphql
mutation {
  createTask(input: {
    title: "New Task"
    status: TO_DO
    priority: MED
  }) {
    id
    title
    status
    createdAt
  }
}
```

Both interfaces are publicly accessible and require no authentication to load, though the GraphQL endpoint and REST API endpoints require JWT tokens for operations.

## Configuration

Application configuration is in `src/main/resources/application.yml`. Key settings:

- **Database**: Configured for PostgreSQL
- **JWT Secret**: Set via `JWT_SECRET` environment variable (default provided for development)
- **Server Port**: 8080 (configurable)
- **Flyway**: Enabled for automatic database migrations

## API Documentation

### Authentication

First, obtain a JWT token:

```bash
POST /api/auth/login
Content-Type: application/json

{
  "username": "testuser",
  "password": "password"
}
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

Use the token in subsequent requests:
```
Authorization: Bearer <token>
```

### Task Endpoints

#### List Tasks
```http
GET /api/tasks?status=TO_DO&page=0&size=20&sort=createdAt,desc
Authorization: Bearer <token>
```

Query Parameters:
- `status` (optional): Filter by status (TO_DO, IN_PROGRESS, DONE)
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 20)
- `sort` (optional): Sort fields (e.g., `createdAt,desc` or `title,asc`)

#### Get Task by ID
```http
GET /api/tasks/{id}
Authorization: Bearer <token>
```

#### Create Task
```http
POST /api/tasks
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "Task Title",
  "description": "Task description",
  "status": "TO_DO",
  "priority": "MED"
}
```

#### Update Task (Full)
```http
PUT /api/tasks/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "Updated Title",
  "description": "Updated description",
  "status": "IN_PROGRESS",
  "priority": "HIGH"
}
```

#### Partial Update Task
```http
PATCH /api/tasks/{id}
Authorization: Bearer <token>
Content-Type: application/merge-patch+json

{
  "status": "DONE"
}
```

#### Delete Task
```http
DELETE /api/tasks/{id}
Authorization: Bearer <token>
```

### Task Model

```json
{
  "id": 1,
  "title": "Task Title",
  "description": "Task description",
  "status": "TO_DO",
  "priority": "MED",
  "version": 0,
  "createdAt": "2024-01-01T10:00:00",
  "updatedAt": "2024-01-01T10:00:00"
}
```

**Status Values**: `TO_DO`, `IN_PROGRESS`, `DONE`
**Priority Values**: `LOW`, `MED`, `HIGH`

## WebSocket Notifications

The API emits WebSocket events to `/topic/tasks` when tasks are created, updated, or deleted.

### Connect to WebSocket

```javascript
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
  console.log('Connected: ' + frame);
  
  stompClient.subscribe('/topic/tasks', function(message) {
    const event = JSON.parse(message.body);
    console.log('Event:', event.eventType, event.payload);
  });
});
```

### Event Types

- `CREATED`: Task created
- `UPDATED`: Task updated
- `DELETED`: Task deleted

## Testing

### Run All Tests
```bash
./gradlew test
```

### Run Unit Tests Only
```bash
./gradlew test --tests "*Test"
```

### Run Integration Tests
```bash
./gradlew test --tests "*IntegrationTest"
```

### Test Coverage

Generate test coverage report:
```bash
./gradlew jacocoTestReport
```

View report: `build/reports/jacoco/test/html/index.html`

## Monitoring & Health Checks

### Health Endpoint
```http
GET /actuator/health
```

### Prometheus Metrics
```http
GET /actuator/prometheus
```

## Docker Commands

### Build Docker Image
```bash
docker build -t kanban-api:1.0.0 .
```

### Run with Docker Compose
```bash
docker-compose up -d
```

### View Logs
```bash
docker-compose logs -f app
```

### Stop Services
```bash
docker-compose down
```

### Stop and Remove Volumes
```bash
docker-compose down -v
```

## Project Structure

```
src/
├── main/
│   ├── java/com/kanban/
│   │   ├── config/          # Configuration classes
│   │   ├── controller/      # REST controllers
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── entity/          # JPA entities
│   │   ├── exception/       # Exception handling
│   │   ├── mapper/          # MapStruct mappers
│   │   ├── model/           # Enums and models
│   │   ├── repository/      # JPA repositories
│   │   ├── security/        # Security configuration
│   │   └── service/         # Business logic
│   └── resources/
│       ├── db/migration/   # Flyway migrations
│       └── application.yml  # Application configuration
└── test/
    └── java/com/kanban/
        ├── integration/     # Integration tests
        ├── mapper/          # Mapper tests
        └── service/          # Service tests
```

## Security

- All `/api/**` endpoints require JWT authentication
- `/swagger-ui.html` and `/v3/api-docs/**` are publicly accessible
- JWT tokens expire after 24 hours (configurable)
- CORS is enabled for cross-origin requests

## Performance

- Response time for `GET /api/tasks?page=0&size=50` is ≤ 150ms on local laptop
- Database indexes on `status` and `created_at` columns
- Pagination implemented for efficient data retrieval

## License

This project is part of a Spring Boot assignment.

## Contributing

This is an assignment project. For questions or issues, please contact the project maintainer.

