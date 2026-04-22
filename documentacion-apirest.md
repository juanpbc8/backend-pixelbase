# Documentación técnica — Proyecto PixelBase

Este documento describe cómo se han implementado los puntos del sílabo en los proyectos `backend-pixelbase` (Spring Boot + PostgreSQL) y `pixelbase-frontend` (Angular).

---

## Índice

- [6.1. Implementación de la API RESTful con Spring Boot](#61-implementación-de-la-api-restful-con-spring-boot)
- [6.2. Back-end con Bases de Datos](#62-back-end-con-bases-de-datos)
- [6.3. Desarrollo del Front-end con Angular](#63-desarrollo-del-front-end-con-angular)
- [6.4. Integración Back-end y Front-end](#64-integración-back-end-y-front-end)

---

## 6.1. Implementación de la API RESTful con Spring Boot

### Configuración del proyecto Spring Boot

El proyecto se construyó con **Spring Initializr** usando **Spring Boot 3.5.13** y **Java 21**. La gestión de dependencias se realiza con **Maven**, y el proyecto incluye un **Maven Wrapper** (`mvnw.cmd` / `mvnw`) para que pueda compilarse sin necesidad de instalar Maven localmente.

Dependencias principales declaradas en `pom.xml`:

- `spring-boot-starter-web` — Servidor embebido Tomcat y soporte REST.
- `spring-boot-starter-data-jpa` — Persistencia con Hibernate.
- `spring-boot-starter-security` — Seguridad y filtros.
- `spring-boot-starter-validation` — Validación de DTOs con Jakarta Bean Validation.
- `postgresql` — Driver JDBC para PostgreSQL.
- `jjwt-api`, `jjwt-impl`, `jjwt-jackson` — Generación y validación de tokens JWT.
- `springdoc-openapi-starter-webmvc-ui` — Documentación OpenAPI y Swagger UI.
- `lombok` — Reducción de boilerplate (constructores, getters, setters).

La configuración se divide en dos archivos:
- `application.properties` — Configuración base pública (con placeholders `${...}`).
- `application-dev.properties` — Credenciales locales (ignorado en Git).

El perfil activo por defecto es `dev`, definido con `spring.profiles.default=dev`.

---

### Creación de endpoints y controladores

Los controladores REST se ubican en `src/main/java/com/pixelbase/backend/**/controller/`. Cada uno se anota con `@RestController` y `@RequestMapping` para agrupar rutas bajo un prefijo común.

Controladores implementados:

| Controlador | Ruta base | Responsabilidad |
|---|---|---|
| [AuthController](src/main/java/com/pixelbase/backend/modules/security/controller/AuthController.java) | `/api/v1/auth` | Registro e inicio de sesión |
| [ProductController](src/main/java/com/pixelbase/backend/catalog/controller/ProductController.java) | `/api/products` | CRUD de productos con paginación y búsqueda |
| [CategoryController](src/main/java/com/pixelbase/backend/catalog/controller/CategoryController.java) | `/api/categories` | CRUD de categorías jerárquicas |
| [AdminController](src/main/java/com/pixelbase/backend/modules/admin/controller/AdminController.java) | `/api/v1/admin` | Endpoints administrativos (estadísticas, gestión de usuarios) |

Ejemplo de endpoint (`AuthController.login`):

```java
@PostMapping("/login")
public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
    return ResponseEntity.ok(authService.login(request));
}
```

El uso de `@Valid` activa la validación automática del DTO antes de ejecutar el método, y `@RequestBody` mapea el JSON entrante al objeto Java.

---

### Implementación de inyección de dependencias

La inyección de dependencias se realiza **por constructor**, que es el patrón recomendado por Spring por su inmutabilidad y facilidad de testeo.

Dos estilos utilizados en el proyecto:

**1. Constructor explícito** (ejemplo en `ProductController`):
```java
private final ProductService productService;

public ProductController(ProductService productService) {
    this.productService = productService;
}
```

**2. Constructor generado por Lombok** (ejemplo en `AuthController`):
```java
@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
}
```

La anotación `@RequiredArgsConstructor` de Lombok genera automáticamente el constructor con todos los campos `final`, evitando repetir código.

Además, las capas están desacopladas mediante **interfaces de servicio**:
- `ProductService` (interfaz) → `ProductServiceImpl` (implementación)
- `CategoryService` (interfaz) → `CategoryServiceImpl` (implementación)

Esto permite sustituir la implementación por un `mock` en los tests sin cambiar el código productivo.

---

### Desarrollo guiado por pruebas (TDD): pruebas unitarias y de integración

El proyecto incluye **29 tests** ubicados en `src/test/java/`, que combinan pruebas unitarias con Mockito y pruebas de integración con Spring Boot.

**Pruebas unitarias** (aislan la lógica de negocio usando mocks):

| Clase de test | Cantidad | Qué prueba |
|---|---|---|
| [AuthServiceTest](src/test/java/com/pixelbase/backend/modules/security/service/AuthServiceTest.java) | 3 | Login correcto, registro, conflicto por email duplicado |
| [ProductServiceImplTest](src/test/java/com/pixelbase/backend/catalog/service/ProductServiceImplTest.java) | 9 | Búsqueda paginada, CRUD, validación de categorías |
| [CategoryServiceImplTest](src/test/java/com/pixelbase/backend/catalog/service/CategoryServiceImplTest.java) | 6 | CRUD completo de categorías |

Ejemplo de test unitario con Mockito:

```java
@Test
@DisplayName("login autentica y devuelve AuthResponse con token")
void loginReturnsAuthResponse() {
    when(authenticationManager.authenticate(any())).thenReturn(authentication);
    when(jwtService.generateToken(principal)).thenReturn("jwt-token-123");

    AuthResponse response = authService.login(new LoginRequest("test@pixelbase.io", "pwd"));

    assertThat(response.token()).isEqualTo("jwt-token-123");
}
```

**Pruebas de controlador** usando `@WebMvcTest` y `MockMvc`:

La clase [ProductControllerTest](src/test/java/com/pixelbase/backend/catalog/controller/ProductControllerTest.java) carga únicamente la capa web y mockea el servicio, verificando que los endpoints responden con los códigos HTTP correctos y manejan validaciones:

```java
mockMvc.perform(get("/api/products/99"))
    .andExpect(status().isNotFound());
```

**Pruebas de integración** end-to-end:

[AuthIntegrationTest](src/test/java/com/pixelbase/backend/integration/AuthIntegrationTest.java) arranca el contexto completo con `@SpringBootTest(webEnvironment = RANDOM_PORT)` y utiliza una base de datos **H2 en memoria** para no afectar la base real. Prueba el flujo: registro → login → acceso a un endpoint protegido con el token obtenido.

Para ejecutar todos los tests:
```bash
./mvnw.cmd test
```

Resultado esperado: `Tests run: 29, Failures: 0, Errors: 0, BUILD SUCCESS`.

---

### Pruebas de API REST con Postman

El proyecto incluye una colección lista para importar en Postman: [postman/PixelBase.postman_collection.json](postman/PixelBase.postman_collection.json).

Esta colección contiene tres carpetas:

- **Auth** — `POST /register` y `POST /login`.
- **Categories** — Operaciones CRUD.
- **Products** — Operaciones CRUD con paginación y filtro.

Al hacer login, un **script post-response** guarda automáticamente el token JWT en la variable `{{token}}` de la colección. Todas las peticiones siguientes lo usan sin intervención manual.

Adicionalmente, se genera una especificación **OpenAPI 3** completa en [postman/api-docs.json](postman/api-docs.json), que puede importarse en:

- **Postman** directamente.
- **Swagger Editor online** (https://editor.swagger.io/) sin necesidad de correr el backend.
- **Swagger UI local** disponible en `http://localhost:8080/swagger-ui.html` mientras el backend está activo.

---

## 6.2. Back-end con Bases de Datos

### Modelado de la Base de Datos

El modelo sigue un diseño **por dominio**, donde cada entidad representa una tabla en PostgreSQL.

Entidades implementadas:

| Entidad | Tabla | Responsabilidad |
|---|---|---|
| [UserEntity](src/main/java/com/pixelbase/backend/modules/user/domain/UserEntity.java) | `users` | Datos del usuario con rol (ADMIN / CUSTOMER) |
| [ProductEntity](src/main/java/com/pixelbase/backend/catalog/entity/ProductEntity.java) | `products` | Producto del catálogo |
| [CategoryEntity](src/main/java/com/pixelbase/backend/catalog/entity/CategoryEntity.java) | `categories` | Categoría con jerarquía padre-hijo |
| [ProductImageEntity](src/main/java/com/pixelbase/backend/catalog/entity/ProductImageEntity.java) | `product_images` | Imágenes asociadas a un producto |

Todas heredan de una **superclase `@MappedSuperclass`** llamada [AuditableEntity](src/main/java/com/pixelbase/backend/common/entity/AuditableEntity.java) que añade los campos `createdAt` y `updatedAt` poblados automáticamente por Spring Data JPA Auditing.

Relaciones entre entidades:

- `Product ↔ Category`: muchos-a-muchos (`@ManyToMany`) con tabla intermedia `product_categories`.
- `Product → ProductImage`: uno-a-muchos (`@OneToMany`) con `orphanRemoval = true`.
- `Category → Category`: auto-referencia (`@ManyToOne` / `@OneToMany`) para subcategorías.

---

### Configuración de JPA y Hibernate

JPA se activa automáticamente al incluir `spring-boot-starter-data-jpa`. La configuración en [application-dev.properties](src/main/resources/application-dev.properties) incluye:

```properties
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

- `ddl-auto=update` — Hibernate crea y actualiza el esquema automáticamente a partir de las anotaciones de las entidades.
- `show-sql` y `format_sql` — imprimen las consultas SQL en consola (útil para depuración).

La auditoría (timestamps automáticos) se habilita con `@EnableJpaAuditing` en una clase de configuración dedicada: [JpaConfig](src/main/java/com/pixelbase/backend/common/config/JpaConfig.java).

---

### Implementación de operaciones CRUD

Los repositorios extienden `JpaRepository`, que provee los métodos estándar (`save`, `findAll`, `findById`, `delete`, etc.) sin escribir código SQL.

Ejemplo: [ProductRepository](src/main/java/com/pixelbase/backend/catalog/repository/ProductRepository.java)

```java
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
    Page<ProductEntity> findByNameContainingIgnoreCase(String keyword, Pageable pageable);
    Boolean existsByNameIgnoreCase(String name);
}
```

Los métodos `findByNameContaining...` y `existsBy...` son **query methods** de Spring Data: Spring genera la consulta SQL automáticamente a partir del nombre del método.

La capa de servicio ([ProductServiceImpl](src/main/java/com/pixelbase/backend/catalog/service/impl/ProductServiceImpl.java)) orquesta la lógica de negocio: valida la entrada, delega en el repositorio y lanza excepciones cuando el recurso no existe.

---

### Uso de JPQL para consultas personalizadas

Cuando los *query methods* no son suficientes, se usa **JPQL** (Java Persistence Query Language) con la anotación `@Query`. Este lenguaje trabaja sobre las entidades (no sobre tablas), por lo que es portable entre motores de base de datos.

Ejemplo real del proyecto en `ProductRepository`:

```java
@Query("SELECT p FROM ProductEntity p " +
       "LEFT JOIN FETCH p.categories " +
       "LEFT JOIN FETCH p.images " +
       "WHERE p.id = :productId")
Optional<ProductEntity> findDetailedById(Long productId);
```

Esta consulta utiliza `JOIN FETCH` para traer en una sola query el producto con sus categorías e imágenes asociadas, evitando el problema de **N+1 queries** al cargar colecciones perezosas.

---

### Gestión de transacciones

Cada clase de servicio se anota con `@Transactional` a nivel de clase. Esto envuelve cada método público en una transacción: si ocurre una excepción, todos los cambios se revierten automáticamente.

Ejemplo en [CategoryServiceImpl](src/main/java/com/pixelbase/backend/catalog/service/impl/CategoryServiceImpl.java):

```java
@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {
    // métodos CRUD
}
```

Ventajas:
- No hace falta llamar a `commit()` o `rollback()` manualmente.
- Las operaciones que modifican varias entidades son atómicas.
- Las entidades cargadas dentro del método permanecen "attached" al contexto de persistencia, permitiendo cargar relaciones perezosas.

---

### Configuración de Spring Security: roles, permisos y autenticación con JWT

La seguridad se configura en [SecurityConfig](src/main/java/com/pixelbase/backend/modules/security/config/SecurityConfig.java) y se compone de tres piezas:

**1. Política de rutas**

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/v1/auth/login", "/api/v1/auth/register").permitAll()
    .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
    .requestMatchers("/api/v1/store/**").hasRole("CUSTOMER")
    .anyRequest().authenticated())
```

**2. Sesiones sin estado** (stateless, basadas solo en JWT):

```java
.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
```

**3. Filtro JWT personalizado**

[JwtAuthenticationFilter](src/main/java/com/pixelbase/backend/modules/security/jwt/JwtAuthenticationFilter.java) intercepta cada petición, extrae el token del header `Authorization: Bearer <token>`, lo valida con [JwtService](src/main/java/com/pixelbase/backend/modules/security/jwt/JwtService.java) y carga el usuario en el `SecurityContext`.

**Roles implementados:**

El enum `Role` define `ADMIN` y `CUSTOMER`. Al registrarse, un usuario obtiene `CUSTOMER` por defecto. El rol se incluye como *claim* dentro del JWT y se usa para autorizar el acceso a rutas restringidas.

**Endpoints del administrador:**

[AdminController](src/main/java/com/pixelbase/backend/modules/admin/controller/AdminController.java) expone operaciones que requieren rol ADMIN:

- `GET /api/v1/admin/stats` — Totales de usuarios, productos y categorías.
- `GET /api/v1/admin/users` — Lista completa de usuarios.
- `PUT /api/v1/admin/users/{id}/promote` — Promueve un usuario a ADMIN.

**Contraseñas seguras:** las contraseñas se almacenan como hash usando `BCryptPasswordEncoder`, no en texto plano.

---

## 6.3. Desarrollo del Front-end con Angular

### Configuración del proyecto Angular

El frontend fue generado con **Angular CLI 20** usando la arquitectura moderna de **standalone components** (sin NgModules). El bootstrap se realiza con `bootstrapApplication()` en [main.ts](../pixelbase-frontend/src/main.ts).

La configuración de providers globales está en [app.config.ts](../pixelbase-frontend/src/app/app.config.ts):

```typescript
export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor]))
  ]
};
```

La URL del backend se centraliza en [environments/environment.ts](../pixelbase-frontend/src/environments/environment.ts):

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080'
};
```

---

### Generación de componentes y uso de SASS para estilos

Los componentes están organizados en dos carpetas:

- `src/app/components/` — Componentes reutilizables (Header, Footer, Hero, Categories).
- `src/app/pages/` — Páginas con su propia ruta (Home, Login, Register, Dashboard).

**SASS** se usa en todos los archivos de estilos. Tanto los globales (`src/styles.scss`) como los de cada componente tienen extensión `.scss`, y `angular.json` está configurado para que `ng generate component` cree por defecto archivos SASS:

```json
"schematics": {
  "@schematics/angular:component": {
    "style": "scss"
  }
}
```

Ejemplo de uso de funcionalidades SASS en [login.scss](../pixelbase-frontend/src/app/pages/login/login.scss):

```scss
$primary: #ff6b35;
$danger: #d33;

.login-form {
  .input-wrapper input {
    border: 1px solid $border;
    &:focus {
      outline: none;
      border-color: $primary;
    }
  }

  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
}
```

Se aprovechan: **variables** (`$primary`), **anidamiento** (*nesting*) de selectores, **selector padre** (`&:focus`) y **media queries anidadas**.

---

### Implementación de rutas y comunicación entre componentes

Las rutas se definen en [app.routes.ts](../pixelbase-frontend/src/app/app.routes.ts):

```typescript
export const routes: Routes = [
  { path: '', component: Home },
  { path: 'login', component: Login },
  { path: 'register', component: Register },
  { path: 'dashboard', component: Dashboard, canActivate: [authGuard] },
  { path: '**', redirectTo: '' }
];
```

La ruta `/dashboard` está protegida por un **guard funcional** [authGuard](../pixelbase-frontend/src/app/guards/auth.guard.ts) que redirige a `/login` si el usuario no está autenticado.

La **comunicación entre componentes** se hace mediante **servicios inyectables** declarados con `@Injectable({ providedIn: 'root' })`. Por ejemplo, cualquier componente puede leer el usuario actual inyectando `AuthService` y accediendo a su signal `currentUser()`.

---

### Procesamiento de formularios y validación de datos

El proyecto utiliza **Reactive Forms** (la aproximación recomendada por Angular para formularios complejos) en los componentes de autenticación.

[Login](../pixelbase-frontend/src/app/pages/login/login.ts) usa `FormBuilder`, `FormGroup` y `Validators`:

```typescript
form: FormGroup = this.fb.group({
  email: ['', [Validators.required, Validators.email]],
  password: ['', [Validators.required, Validators.minLength(12)]],
});
```

[Register](../pixelbase-frontend/src/app/pages/register/register.ts) añade un **validador personalizado** a nivel de grupo que verifica que la confirmación de contraseña coincida:

```typescript
private passwordsMatch(group: AbstractControl): ValidationErrors | null {
  const p = group.get('password')?.value;
  const c = group.get('confirmPassword')?.value;
  return p && c && p !== c ? { mismatch: true } : null;
}
```

En la plantilla se usan las sintaxis `[formGroup]` y `formControlName`, y los mensajes de error contextuales se muestran cuando el campo ha sido tocado y es inválido:

```html
@if (email.touched && email.invalid) {
  <small class="field-error">
    @if (email.errors?.['required']) { Email es obligatorio }
    @else if (email.errors?.['email']) { Formato de email inválido }
  </small>
}
```

---

### Consumo de la API REST desde Angular

La comunicación con el backend se hace a través de tres servicios tipados que usan `HttpClient`:

| Servicio | Responsabilidad |
|---|---|
| [AuthService](../pixelbase-frontend/src/app/services/auth.service.ts) | `login()`, `register()`, `logout()`, `getToken()`, `isAuthenticated()` |
| [ProductService](../pixelbase-frontend/src/app/services/product.service.ts) | CRUD de productos con paginación y búsqueda por `q` |
| [CategoryService](../pixelbase-frontend/src/app/services/category.service.ts) | CRUD de categorías |

Ejemplo de llamada tipada:

```typescript
login(body: LoginRequest): Observable<AuthResponse> {
  return this.http.post<AuthResponse>(`${this.base}/login`, body).pipe(
    tap((res) => this.persist(res))
  );
}
```

Las interfaces (`LoginRequest`, `AuthResponse`, `Product`, `Category`) están en `src/app/models/` y reflejan los DTOs del backend.

---

### Integración de autenticación y autorización con JWT

La autenticación se basa en un **interceptor HTTP** global: [authInterceptor](../pixelbase-frontend/src/app/interceptors/auth.interceptor.ts). Este añade automáticamente el header `Authorization: Bearer <token>` a cada petición saliente si el usuario tiene una sesión activa:

```typescript
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = inject(AuthService).getToken();
  if (!token) return next(req);
  return next(req.clone({
    setHeaders: { Authorization: `Bearer ${token}` }
  }));
};
```

El token y los datos del usuario se guardan en `localStorage` tras el login exitoso, de modo que la sesión persiste al recargar la página. El `AuthService` expone un **Angular signal** `currentUser()` para que cualquier componente reaccione a cambios de sesión.

La autorización se aplica a nivel de ruta con el `authGuard` (visto antes en la sección de rutas).

---

## 6.4. Integración Back-end y Front-end

### Comunicación entre Spring Boot y Angular

La integración se logra por tres piezas que trabajan juntas:

**1. CORS** — El backend autoriza las peticiones provenientes del frontend mediante [SecurityConfig](src/main/java/com/pixelbase/backend/modules/security/config/SecurityConfig.java):

```java
c.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
c.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
c.setAllowCredentials(true);
```

El valor `allowedOrigins` se lee de la variable `CORS_ALLOWED_ORIGINS`, que en desarrollo incluye `http://localhost:4200`.

**2. Contrato compartido** — Los DTOs del backend (`LoginRequest`, `AuthResponse`, `ProductRequest`, etc.) tienen su **espejo** en las interfaces TypeScript del frontend. Esto se documenta y se puede regenerar automáticamente desde [api-docs.json](postman/api-docs.json).

**3. Intercambio del JWT** — Flujo completo:

```
Frontend: POST /api/v1/auth/login  →  Backend responde { token, email, role }
Frontend: guarda token en localStorage
Frontend: authInterceptor añade "Authorization: Bearer ..." a cada request
Backend:  JwtAuthenticationFilter valida el token
Backend:  SecurityConfig aplica reglas por rol
```

Para verificar la integración en local:

```bash
# Terminal 1
cd backend-pixelbase
./mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"

# Terminal 2
cd pixelbase-frontend
npm start
```

Y abrir el navegador en `http://localhost:4200/register` para registrar una cuenta y acceder al dashboard.

---

### Pruebas de integración

Las pruebas de integración viven en el backend y utilizan `@SpringBootTest` con un servidor real y una base **H2 en memoria**, independiente de PostgreSQL.

Archivo: [AuthIntegrationTest](src/test/java/com/pixelbase/backend/integration/AuthIntegrationTest.java)

Casos cubiertos:

1. **Registro crea usuario y devuelve JWT** — valida que la persistencia, la generación de token y la respuesta HTTP funcionen juntas.
2. **Login con credenciales válidas devuelve token** — comprueba la autenticación contra un usuario previamente registrado.
3. **Endpoint protegido sin token devuelve 401** — valida que Spring Security rechace peticiones sin autenticación.
4. **Endpoint protegido con JWT válido devuelve 200** — cierra el ciclo: el token emitido por el mismo backend es aceptado por el filtro de seguridad.

Ejemplo del test que valida el flujo completo:

```java
// 1. Registrar usuario
ResponseEntity<AuthResponse> auth = restTemplate.postForEntity(
    "/api/v1/auth/register",
    new RegisterRequest("flow@pixelbase.io", "PasswordSeguro12!"),
    AuthResponse.class);

String token = auth.getBody().token();

// 2. Llamar endpoint protegido con el token
HttpHeaders headers = new HttpHeaders();
headers.setBearerAuth(token);

ResponseEntity<String> response = restTemplate.exchange(
    "/api/products", HttpMethod.GET, new HttpEntity<>(headers), String.class);

assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
```

Estas pruebas se ejecutan junto con las unitarias al correr `./mvnw.cmd test`.

---

## Resumen de archivos clave

**Backend:**
- [pom.xml](pom.xml) — Dependencias y build.
- [SecurityConfig.java](src/main/java/com/pixelbase/backend/modules/security/config/SecurityConfig.java) — Seguridad, CORS, reglas por rol.
- [JwtService.java](src/main/java/com/pixelbase/backend/modules/security/jwt/JwtService.java) — Generación y validación de tokens.
- [GlobalExceptionHandler.java](src/main/java/com/pixelbase/backend/common/exception/GlobalExceptionHandler.java) — Manejo centralizado de errores.
- [AuditableEntity.java](src/main/java/com/pixelbase/backend/common/entity/AuditableEntity.java) — Timestamps automáticos.

**Frontend:**
- [app.config.ts](../pixelbase-frontend/src/app/app.config.ts) — Providers globales.
- [app.routes.ts](../pixelbase-frontend/src/app/app.routes.ts) — Rutas y guards.
- [auth.service.ts](../pixelbase-frontend/src/app/services/auth.service.ts) — Autenticación.
- [auth.interceptor.ts](../pixelbase-frontend/src/app/interceptors/auth.interceptor.ts) — Inyección automática del JWT.

**Documentación y pruebas:**
- [postman/PixelBase.postman_collection.json](postman/PixelBase.postman_collection.json) — Colección Postman lista.
- [postman/api-docs.json](postman/api-docs.json) — Especificación OpenAPI.
- Swagger UI disponible en `http://localhost:8080/swagger-ui.html`.
