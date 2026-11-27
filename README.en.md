# learning-accumulation

English | [简体中文](README.md)

`learning-accumulation` is an actively maintained, separated frontend/backend administration platform for authorization, workflow, knowledge management, and reusable business modules. It is built with Spring Boot 3.5, MyBatis-Plus, Sa-Token, Warm-Flow, and Spring AI, with support for PostgreSQL 17 and MySQL 8.

Companion frontend: [learning-accumulation-vue](https://gitee.com/438704315/learning-accumulation-vue)

> The project is under active development and is intended for learning, extension, and feature evaluation. Before production use, assess testing, security, monitoring, backup, and release requirements. See the [production readiness checklist](docs/production-readiness-checklist.md).

## Goals

- Provide reusable user, role, menu, department, post, and dictionary management.
- Provide visual workflow design, task processing, and process instance management.
- Explore practical AI chat, document summarization, and knowledge-base Q&A workflows.
- Keep the backend API decoupled from the Vue administration client.

## Features

| Module | Capabilities |
| --- | --- |
| Authentication and RBAC | Sa-Token authentication; users, roles, menus, departments, posts, and dynamic permissions |
| Workflow | Categories, definitions, visual design, instances, tasks, copies, and a leave-request example |
| Knowledge base | Markdown articles, revision history, annotations, file import, AI summaries, and article Q&A |
| AI assistant | Basic chat through Spring AI and an OpenAI-compatible endpoint |
| File center | Upload, batch lookup, preview, download, and extension/MIME validation |
| Content management | CMS content APIs and rich-text content support |
| Photo albums | Album, image, and video management; tags, move/copy, and offline export |
| Code generation | Database introspection, generator configuration, preview, and download |
| Auditing | Login and operation logs, trace context, slow-query and index auditing |
| Operations | Actuator health checks and environment-controlled OpenAPI, Druid, and Warm-Flow UI |

## Technology

| Area | Stack |
| --- | --- |
| Core | Java 17, Spring Boot 3.5.9 |
| Persistence | MyBatis-Plus 3.5.16, Druid, dynamic datasource |
| Storage | PostgreSQL 17 / MySQL 8, Redis |
| Authentication | Sa-Token |
| Workflow | Warm-Flow 1.8.5 |
| AI | Spring AI 1.1.2, OpenAI-compatible API |
| API documentation | Springdoc OpenAPI |
| Utilities | MapStruct Plus, Lombok, Hutool |

The frontend uses Vue 3, TypeScript, Vite, Element Plus, Pinia, and Vue Router. See [learning-accumulation-vue](https://gitee.com/438704315/learning-accumulation-vue) for exact versions.

## Quick Start

### Requirements

- JDK 17
- PostgreSQL 17 or MySQL 8
- Redis 6+
- Node.js 20.19+ or 22.12+ for the frontend

### Clone

```bash
git clone https://gitee.com/438704315/learning-accumulation.git
git clone https://gitee.com/438704315/learning-accumulation-vue.git
```

### Initialize PostgreSQL

```bash
createdb h_boot
psql -X -d h_boot -f sql/postgresql/import.sql
```

For MySQL, import `sql/h_boot.sql` and provide the matching JDBC environment variables.

### Configure and run the backend

See [.env.example](.env.example) for the complete configuration surface.

```bash
export SPRING_PROFILES_ACTIVE=dev
export HBOOT_DB_URL='jdbc:postgresql://localhost:5432/h_boot'
export HBOOT_DB_USERNAME='your-user'
export HBOOT_DB_PASSWORD='your-password'
export ENGINE_DB_URL="$HBOOT_DB_URL"
export ENGINE_DB_USERNAME="$HBOOT_DB_USERNAME"
export ENGINE_DB_PASSWORD="$HBOOT_DB_PASSWORD"
export REDIS_HOST='localhost'
export REDIS_PORT='6379'
./mvnw spring-boot:run
```

AI is optional for the core application. Configure `AI_BASE_URL`, `AI_API_KEY`, and `AI_MODEL` only when using AI features.

Development endpoints:

- API: `http://localhost:8080`
- Health: `http://localhost:8080/actuator/health`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

### Run the frontend

```bash
cd ../learning-accumulation-vue
npm install
npm run dev
```

The frontend runs at `http://localhost:5173` and proxies API requests to port `8080`.

## Repository Layout

```text
src/main/java/com/quinn/hboot/
├── ai/          # AI chat
├── codegen/     # Code generation
├── common/      # Shared models, exceptions, logging, and tracing
├── knowledge/   # Knowledge base
├── photo/       # Albums and media
├── system/      # Authentication, RBAC, and system administration
└── workflow/    # Workflow
```

## Documentation

- [Environment and startup guide](docs/environment-and-startup.md)
- [Production readiness checklist](docs/production-readiness-checklist.md)
- [AI integration plan](docs/ai-integration-plan.md)
- [Photo album implementation plan](docs/family-album-implementation-plan.md)
- [PostgreSQL initialization guide](sql/postgresql/README.md)

## Project History and Roadmap

The project has been publicly maintained since December 2025. Major milestones include system administration, file and content management, workflow design and processing, Sa-Token migration, AI integration, a knowledge base, and photo albums.

Current priorities are automated coverage for critical authentication and workflow paths, database migration practices, production observability, data permissions, object-storage adapters, and improved knowledge retrieval. Roadmap items describe direction and are not release commitments.

## Contributing

Issues and pull requests are welcome.

1. Fork the repository and create a focused branch from `master`.
2. Include tests or verification notes for important behavior.
3. Run `./mvnw test` before submitting.
4. Describe the problem, implementation, and verification in the pull request.

Do not disclose real secrets, production endpoints, or sensitive data in public issues.

## Acknowledgements

This project uses and learns from open-source projects including Spring Boot, MyBatis-Plus, Sa-Token, Warm-Flow, Spring AI, Vue, and Element Plus. Some administration-system design ideas were informed by [RuoYi-Vue-Plus](https://gitee.com/dromara/RuoYi-Vue-Plus). Users and distributors must follow this project's license and the licenses of its upstream dependencies.

## License

Licensed under the [Apache License 2.0](LICENSE).
