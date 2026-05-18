---
name: java-springboot-pixelbase
description: "Strict clean code and architectural standards for Pixelbase Modular Monolith (Java 21 / Spring Boot 3.5.x)."
---

# Pixelbase Core Skills & Standards

## 1. Architecture & Repository Layout

- **Module Boundaries:** Split strictly into `com.pixelbase.backend.common` (cross-cutting infra) and
  `com.pixelbase.backend.modules.[feature]` (business domains). No layered packages at the root.
- **Package-by-Feature:** Inside any business module, use only these sub-packages: `controller`, `service`,
  `repository`, `domain`, `dto`, `mapper`, `seed`.
- **Inter-Module Communication:** Synchronous via public interfaces and services only. Business modules must never
  import classes from another module's internal packages directly.
- **Security Architecture:** Authentication and authorization are stateless JWT-based using the `jjwt` library. All
  security filters, configurations, and utilities reside strictly within `com.pixelbase.backend.modules.security`.

## 2. Configuration & Secrets

- **Format:** Strictly use properties files (`application-{profile}.properties`). Do NOT use YAML (`.yml`).
- **Type-Safe Configuration:** Bind properties to structured Java objects using `@ConfigurationProperties` combined with
  immutable records or classes.
- **Secrets Management:** Never hardcode credentials. Inject them strictly via environment variables.

## 3. Component Stereotypes & Dependency Injection

- **Stereotypes:** Annotate explicitly using `@RestController`, `@Service`, `@Repository`, and `@Component`.
- **Injection:** Strictly use constructor-based injection via Lombok `@RequiredArgsConstructor` on `final` fields. Never
  use field injection (`@Autowired`).

## 4. Web Layer (Controllers)

- **RESTful Design:** Build clean, noun-based RESTful endpoints with appropriate HTTP methods (`GET`, `POST`, `PUT`,
  `DELETE`).
- **Data Isolation:** Always use DTOs (preferably Java `record`) for request and response payloads. NEVER expose JPA
  entities directly to the client.
- **Validation:** Enforce request validation at the controller entrypoint using JSR 380 annotations (`@Valid`,
  `@NotNull`, `@NotBlank`, `@Size`) on DTOs.
- **Errors:** All exceptions must bubble up to the global exception handler in `common.exception` to return structured
  JSON error responses.

## 5. Service Layer (Business Logic)

- **Encapsulation:** All business logic, domain state transitions, and validation rules must live inside `@Service`
  classes. Services must remain stateless.
- **Transactions:** Declaratively manage database boundaries using `@Transactional`. Apply it at the method level and
  use `readOnly = true` for read operations to optimize performance.

## 6. Data Layer (Repositories & Entities)

- **Naming Conventions:** All database tables (`@Table(name = "...")`) and columns (`@Column(name = "...")`) MUST be
  mapped explicitly using **snake_case** (e.g., `product_prices`, `created_at`).
- **Spring Data JPA:** Extend `JpaRepository<T, ID>`. Prefer derived query methods or JPQL (`@Query`) over native SQL
  queries.
- **Advanced Dynamic Queries:** For complex dynamic filtering (e.g., global search by name, price, filters, etc.), use *
  *JPA Criteria API via Spring Data Specifications**. Use
  `com.pixelbase.backend.modules.catalog.repository.specification.ProductSpecification.java` as the implementation
  baseline.
- **Auditing:** Every table requiring history tracking must extend
  `com.pixelbase.backend.common.entity.AuditableEntity`.
- **Projections:** Use DTO projections instead of fetching full entities when performing specific, read-only queries to
  reduce memory footprint.

## 7. Quality, Logging & Testing

- **Optional Handling:** Finders returning nullable data must return `Optional<T>`. Always unwrap values using
  `.orElseThrow(() -> new CustomDomainException(...))` to fail fast with descriptive runtime exceptions. Never use raw
  `.get()`.
- **Streams:** Keep stream pipelines short and readable; avoid complex nested streams.
- **Logging:** Use SLF4J API. Always use parameterized logs (`log.info("action_triggered id={}", id);`) to avoid string
  concatenation overhead.
- **Testing Slices:** Focus tests using slices like `@WebMvcTest` (Controllers) or `@DataJpaTest` (Repositories)
  combined with Mockito. Use Testcontainers for real database integration testing.

## 8. Language & Documentation Standards

- **Source Code:** All code syntax (class names, variables, methods, database tables, and columns) MUST be written
  strictly in **ENGLISH**.
- **Localization Exceptions:** The following elements MUST be written strictly in **SPANISH**:
    - All log messages (`log.info()`, `log.error()`, etc.).
    - Definitions and values of Java `enum` classes.
    - API documentation annotations (Swagger / OpenAPI descriptions and summaries).
    - Inline code documentation and Javadoc comments.
