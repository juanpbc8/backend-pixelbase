# AGENTS.md

## Scope and Architecture
- Modular monolith: keep cross-cutting infra in `common/`, business features in `modules/` (no cross-over).
- Inside `modules/`, use package-by-feature layout (controller, service, repository, domain, dto).
- Module communication is synchronous via interfaces/services only; no event-driven patterns or Spring Modulith tooling.

## Spring/JPA Conventions
- Entities needing history/auditing must extend `com.pixelbase.backend.common.entity.AuditableEntity`.
- JPA auditing is enabled in `com.pixelbase.backend.common.config.JpaConfig`.

## Dependency and Build Source of Truth
- Maven `pom.xml` is the source of truth for dependencies and Java version (Java 21, Spring Boot 3.5.x).

## Local Run and Profiles
- Local dev uses `application-dev.properties` copied from `src/main/resources/application-dev.properties.template` (file is gitignored).
- Run locally with the Maven wrapper: `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`.
- PostgreSQL is required for local development.

## Coding Style
- Prefer constructor injection with Lombok `@RequiredArgsConstructor` on `final` fields; avoid field injection (`@Autowired`).
