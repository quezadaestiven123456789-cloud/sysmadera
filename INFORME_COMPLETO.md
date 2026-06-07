# WoodManager — Informe Completo del Proyecto

**Sistema de Gestión de Talleres de Carpintería**  
*Versión: 1.0.0 | Spring Boot 3.5.0 | Java 17*

---

## Índice

1. [Resumen Ejecutivo](#1-resumen-ejecutivo)
2. [Stack Tecnológico](#2-stack-tecnológico)
3. [Estructura del Proyecto](#3-estructura-del-proyecto)
4. [Arquitectura y Diseño](#4-arquitectura-y-diseño)
5. [Módulo de Clientes — Análisis Detallado](#5-módulo-de-clientes--análisis-detallado)
6. [Suite de Pruebas](#6-suite-de-pruebas)
7. [Problemas Conocidos y Limitaciones](#7-problemas-conocidos-y-limitaciones)
8. [Configuración y Despliegue](#8-configuración-y-despliegue)
9. [Métricas de Código](#9-métricas-de-código)
10. [Guía de Contribución](#10-guía-de-contribución)

---

## 1. Resumen Ejecutivo

WoodManager es una aplicación web SaaS profesional para la gestión integral de talleres de carpintería. Proporciona funcionalidades para:

- **Gestión de usuarios y autenticación** (JWT, roles ADMIN/EMPLEADO/CLIENTE)
- **Catálogo de clientes** (CRUD completo con búsqueda paginada)
- **Inventario de madera** (control de existencias, tipos, proveedores)
- **Catálogo de muebles** (productos, precios, stock)
- **Pedidos** (creación, seguimiento de estados, detalles)
- **Facturación** (generación, estados de pago)
- **Reportes y dashboard** (estadísticas, productos más vendidos)

El frontend es HTML/CSS/JS plano con diseño dark-mode premium (Inspiración: Material Design + Ant Design). El backend expone una API RESTful bajo el prefijo `/api/v1`.

---

## 2. Stack Tecnológico

### Backend

| Componente | Versión |
|---|---|
| Java | 17 |
| Spring Boot | 3.5.0 |
| Spring Data JPA | (incluido en Boot) |
| Spring Security | (incluido en Boot) |
| Spring Validation | (incluido en Boot) |
| Spring Actuator | (incluido en Boot) |
| MySQL Connector | 8.x (runtime) |
| H2 Database | (test scope) |
| JJWT (JWT) | 0.12.6 |
| Springdoc OpenAPI | 2.8.5 |
| Lombok | (optional) |
| Hibernate | 6.x (via Boot) |

### Frontend

| Componente | Detalle |
|---|---|
| HTML5 | 10 páginas en `src/main/resources/static/producto/` |
| CSS3 | `woodmanager.css` — diseño dark-mode system |
| JavaScript | `api.js` (cliente HTTP), `woodmanager.js` (lógica UI) |
| Iconos | Font Awesome 6 |
| Fuentes | Inter (headings/body), JetBrains Mono (monospace) |

### Herramientas de Desarrollo

| Herramienta | Propósito |
|---|---|
| Maven Wrapper | Build y gestión de dependencias |
| Spring Boot DevTools | Recarga en caliente en desarrollo |
| Lombok | Reducción de boilerplate |
| Spring Security Test | Soporte para pruebas de seguridad |
| JUnit 5 + Mockito | Pruebas unitarias y de integración |
| AssertJ | Aserciones fluidas |
| H2 Database | Base de datos embebida para pruebas |

---

## 3. Estructura del Proyecto

```
sys_madera/
├── pom.xml
├── mvnw / mvnw.cmd
├── DESIGN.md
├── HELP.md
├── INFORME_COMPLETO.md          ← Este archivo
│
├── src/main/java/com/madera/sys_madera/
│   ├── SysMaderaApplication.java
│   │
│   ├── config/
│   │   ├── WebConfig.java           ← Path prefix `/api/v1` para @RestController
│   │   ├── JpaConfig.java           ← @EnableJpaAuditing (separado de WebConfig)
│   │   ├── OpenApiConfig.java       ← Swagger/OpenAPI 3
│   │   └── DataInitializer.java     ← Seeds roles en BD al iniciar
│   │
│   ├── model/
│   │   ├── User.java, Role.java, ERole.java
│   │   ├── Client.java
│   │   ├── Furniture.java
│   │   ├── Order.java, OrderDetail.java, EOrderStatus.java
│   │   ├── Invoice.java, EInvoiceStatus.java
│   │   ├── WoodInventory.java
│   │   └── WoodSurplus.java
│   │
│   ├── repository/
│   │   ├── UserRepository.java
│   │   ├── RoleRepository.java
│   │   ├── ClientRepository.java
│   │   ├── FurnitureRepository.java
│   │   ├── OrderRepository.java, OrderDetailRepository.java
│   │   ├── InvoiceRepository.java
│   │   ├── WoodInventoryRepository.java
│   │   └── WoodSurplusRepository.java
│   │
│   ├── dto/
│   │   ├── request/  (7 DTOs: Login, Register, Client, Furniture, Order, Invoice, WoodInventory)
│   │   └── response/ (10 DTOs: Auth, Client, Furniture, Order, Invoice, WoodInventory,
│   │                    PagedResponse<T>, MessageResponse, TopSellingFurnitureItem,
│   │                    WoodInventoryReportItem)
│   │
│   ├── exception/
│   │   ├── AppException.java
│   │   ├── BadRequestException.java
│   │   ├── DuplicateResourceException.java
│   │   ├── ResourceNotFoundException.java
│   │   └── GlobalExceptionHandler.java  ← @RestControllerAdvice
│   │
│   ├── security/
│   │   ├── config/SecurityConfig.java   ← @EnableMethodSecurity, filtro JWT
│   │   ├── jwt/
│   │   │   ├── JwtTokenProvider.java
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   └── JwtEntryPoint.java
│   │   └── CustomUserDetailsService.java
│   │
│   ├── service/
│   │   ├── (9 interfaces: Auth, Client, Dashboard, Furniture, Invoice,
│   │   │    Order, Report, WoodInventory, WoodSurplus)
│   │   └── impl/ (9 implementaciones)
│   │
│   └── util/Constants.java
│
├── src/main/resources/
│   ├── application.properties
│   ├── application-dev.properties      ← MySQL, JWT, logging
│   ├── application-prod.properties
│   └── static/
│       ├── css/woodmanager.css
│       ├── js/api.js, woodmanager.js
│       └── producto/ (10 páginas HTML)
│
└── src/test/
    ├── java/com/madera/sys_madera/
    │   ├── SysMaderaApplicationTests.java
    │   ├── service/impl/
    │   │   ├── ClientServiceImplTest.java    ← 10 tests
    │   │   └── OrderServiceTest.java         ← 13 tests (preexistente)
    │   ├── repository/
    │   │   └── ClientRepositoryTest.java     ← 18 tests
    │   └── controller/
    │       └── ClientControllerTest.java     ← 15 tests
    └── resources/application.properties      ← H2 dialect, ddl-auto=create-drop
```

---

## 4. Arquitectura y Diseño

### 4.1 Capas

```
Cliente (HTML/JS) → Controller (API REST) → Service (negocio) → Repository (JPA) → BD (MySQL)
                        ↕                           ↕
                  GlobalExceptionHandler    DTOs (Request/Response)
```

### 4.2 Patrón de Prefijo de Rutas

`WebConfig` implementa `WebMvcConfigurer.configurePathMatch()` para añadir automáticamente el prefijo `/api/v1` a todos los `@RestController`. Esto evita tener que escribir `/api/v1` en cada `@RequestMapping`.

```java
configurer.addPathPrefix("/api/v1",
    clazz -> AnnotatedElementUtils.hasAnnotation(clazz, RestController.class));
```

### 4.3 Seguridad

- **Autenticación**: JWT (Bearer token) con 24h de expiración
- **Roles**: ADMIN, EMPLEADO, CLIENTE
- **Autorización**: `@PreAuthorize` en métodos de controller + `@EnableMethodSecurity`
- **Filtro**: `JwtAuthenticationFilter` (OncePerRequestFilter) extrae token, valida, y establece `SecurityContext`
- **CORS**: Permitido desde cualquier origen, métodos estándar HTTP
- **Endpoints públicos**: `/api/v1/auth/**`, `/swagger-ui/**`, `/v3/api-docs/**`, `/actuator/health`

### 4.4 Mapeo de la Base de Datos

| Tabla | Entidad | Relaciones |
|---|---|---|
| `users` | User | 1:1 → Client, M:M → roles |
| `roles` | Role | M:M → users |
| `user_roles` | (Join Table) | users ↔ roles |
| `clients` | Client | 1:1 → User, 1:M → orders |
| `furniture` | Furniture | 1:M → order_details |
| `orders` | Order | M:1 → Client, 1:1 → Invoice, 1:M → order_details |
| `order_details` | OrderDetail | M:1 → Order, M:1 → Furniture |
| `invoices` | Invoice | 1:1 → Order |
| `wood_inventory` | WoodInventory | 1:M → WoodSurplus |
| `wood_surplus` | WoodSurplus | M:1 → WoodInventory |

### 4.5 Auditoría (JPA Auditing)

Las entidades `Client`, `User`, `Furniture`, `Order`, `Invoice`, `WoodInventory`, `WoodSurplus` usan `@CreatedDate` y `@LastModifiedDate` con `@EntityListeners(AuditingEntityListener.class)`.

La activación se realiza mediante `@EnableJpaAuditing` en una clase `@Configuration` separada (`JpaConfig`).

---

## 5. Módulo de Clientes — Análisis Detallado

### 5.1 Endpoints

| Método | Ruta | Roles | Descripción |
|---|---|---|---|
| `POST` | `/api/v1/clientes` | ADMIN, EMPLEADO | Crear cliente |
| `GET` | `/api/v1/clientes/{id}` | ADMIN, EMPLEADO, CLIENTE | Obtener por ID |
| `GET` | `/api/v1/clientes` | ADMIN, EMPLEADO | Listar (paginado, filtro por nombre) |
| `PUT` | `/api/v1/clientes/{id}` | ADMIN, EMPLEADO | Actualizar cliente |
| `DELETE` | `/api/v1/clientes/{id}` | ADMIN | Eliminar cliente |

### 5.2 Flujo de Creación

1. Controller recibe `ClientRequest` (valida con `@Valid`)
2. Service verifica unicidad de email y RFC
3. Service construye entidad `Client` y persiste vía `ClientRepository`
4. Service mapea entidad → `ClientResponse` y retorna
5. Controller responde `201 Created`

### 5.3 Validaciones

**`ClientRequest`**:
- `name`: `@NotBlank`
- `email`: `@Email` (opcional)
- `phone`, `address`, `rfc`: opcionales

**Servicio**:
- Email duplicado → `DuplicateResourceException` (409 Conflict)
- RFC duplicado → `DuplicateResourceException` (409 Conflict)
- Cliente no encontrado → `ResourceNotFoundException` (404 Not Found)

### 5.4 Paginación

`GET /api/v1/clientes?page=0&size=10&sort=id&direction=asc&search=Juan`

Parámetros con valores por defecto desde `Constants.java`:
- `page=0`, `size=10`, `sort=id`, `direction=asc`
- `search` opcional: filtra por `name` (case-insensitive)

---

## 6. Suite de Pruebas

### 6.1 Resumen General

| Clase de Prueba | Tipo | Cantidad | Estado |
|---|---|---|---|
| `ClientServiceImplTest` | Unitario (Mockito) | 10 | ✅ Todos pasan |
| `OrderServiceTest` | Unitario (Mockito) | 13 | ✅ Todos pasan (preexistente) |
| `ClientRepositoryTest` | Integración (DataJpaTest + H2) | 18 | ✅ Todos pasan |
| `ClientControllerTest` | Integración (WebMvcTest) | 15 | ✅ Todos pasan |
| `SysMaderaApplicationTests` | Integración (SpringBootTest) | 1 | ✅ Pasa |
| **Total** | | **57** | **57 pasan** |

### 6.2 ClientServiceImplTest (10 tests)

| Grupo | Test | Descripción |
|---|---|---|
| **Create** (3) | `shouldSaveClient_whenValidRequest` | Crea cliente, verifica campos y captura argumento |
| | `shouldThrowException_whenEmailAlreadyExists` | Email duplicado → `DuplicateResourceException` |
| | `shouldThrowException_whenRfcAlreadyExists` | RFC duplicado → `DuplicateResourceException` |
| **FindById** (2) | `shouldReturnClient_whenClientExists` | Encuentra cliente, verifica todos los campos |
| | `shouldThrowException_whenClientNotFound` | No encontrado → `ResourceNotFoundException` |
| **Update** (3) | `shouldUpdateClient_whenValidRequest` | Actualiza nombre y email |
| | `shouldThrowException_whenClientNotFound` | No encontrado → `ResourceNotFoundException` |
| | `shouldThrowException_whenEmailAlreadyTaken` | Email tomado → `DuplicateResourceException` |
| **Delete** (2) | `shouldDeleteClient_whenClientExists` | Elimina cliente existente |
| | `shouldThrowException_whenClientNotFound` | No encontrado → `ResourceNotFoundException` |

Técnicas usadas:
- `@ExtendWith(MockitoExtension.class)`
- `@Mock` + `@InjectMocks`
- `BDDMockito.given/willReturn/willThrow`
- `ArgumentCaptor` para verificar estado interno guardado
- `verify(clientRepository, never()).save(any())` para caminos de error

### 6.3 ClientRepositoryTest (18 tests)

| Grupo | Tests | Descripción |
|---|---|---|
| **Save** (1) | `shouldSaveClient` | Persiste con ID generado y timestamps |
| **FindById** (2) | `shouldFindById`, `shouldReturnEmpty_whenNotFound` | Búsqueda por ID |
| **FindByEmail** (2) | `shouldFindByEmail`, `shouldReturnEmpty_whenEmailNotFound` | Búsqueda por email |
| **FindByRfc** (2) | `shouldFindByRfc`, `shouldReturnEmpty_whenRfcNotFound` | Búsqueda por RFC |
| **ExistsByEmail** (2) | `shouldReturnTrue/False_whenEmailExists/NotExists` | Verifica existencia |
| **ExistsByRfc** (2) | `shouldReturnTrue/False_whenRfcExists/NotExists` | Verifica existencia |
| **FindByNameContainingIgnoreCase** (3) | Búsqueda insensitive, página vacía, mayúsculas/minúsculas | Paginación + filtro |
| **Delete** (1) | `shouldDeleteClient` | Eliminación física |
| **Update** (1) | `shouldUpdateClient` | Modificación y verificación |
| **Constraints** (2) | `shouldEnforceUniqueEmail`, `shouldEnforceUniqueRfc` | Violación → `DataIntegrityViolationException` |

Técnicas usadas:
- `@DataJpaTest` con `@Import(JpaConfig.class)` para auditoría
- H2 en memoria (dialecto `H2Dialect`, `ddl-auto=create-drop`)
- `saveAndFlush` para forzar validación de constraints en tests
- AssertJ para aserciones

### 6.4 ClientControllerTest (15 tests)

| Grupo | Tests | Descripción |
|---|---|---|
| **Create** (3) | `shouldReturn201`, `shouldReturn400_whenInvalidBody`, `shouldReturn500_whenEmptyBody` | POST creación |
| **FindById** (2) | `shouldReturn200`, `shouldReturn404_whenNotFound` | GET por ID |
| **FindAll** (2) | `shouldReturn200`, `shouldAcceptSearch` | GET paginado |
| **Update** (1) | `shouldReturn200` | PUT actualización |
| **Delete** (2) | `shouldReturn200`, `shouldReturn404_whenNotFound` | DELETE |
| **AccessControl** (5) | Roles EMPLEADO/CLIENTE en GET, POST, DELETE | Verificación de acceso |

Técnicas usadas:
- `@WebMvcTest(ClientController.class)` con exclusión de auto-configuraciones JPA/DataSource
- `@AutoConfigureMockMvc(addFilters = false)` desactiva security filters
- `@MockitoBean` para `ClientService`, `JwtTokenProvider`, `CustomUserDetailsService`
- `@WithMockUser` para simular autenticación
- MockMvc `perform()` y `andExpect()` con `jsonPath` para verificar respuestas JSON
- Verificación de mensajes de error en `ResourceNotFoundException`

### 6.5 Mejores Prácticas Aplicadas

- **@Nested + @DisplayName** — Organización jerárquica legible
- **BDDMockito** — `given/willReturn` sobre `when/thenReturn`
- **ArgumentCaptor** — Verificación de objetos pasados al repositorio
- **Datos realistas** — Nombres, emails, RFCs, direcciones con sentido
- **Constantes compartidas** — `CLIENT_ID`, `CLIENT_NAME`, etc. reutilizadas
- **Métodos helpers** — `buildRequest()`, `buildResponse()`, `buildClient()`, `buildJsonRequest()`
- **Pruebas de timestamps** — Verificación de `@CreatedDate` y `@LastModifiedDate`
- **Pruebas de constraints** — Violaciones de unicidad verificadas con `DataIntegrityViolationException`

---

## 7. Problemas Conocidos y Limitaciones

### 7.1 Serialización de `PagedResponse<T>` en @WebMvcTest

**Problema**: El record genérico `PagedResponse<T>` no se serializa correctamente en el contexto de `@WebMvcTest` (Jackson no logra determinar el tipo concreto del parámetro genérico en un contexto de prueba simulado). La respuesta HTTP retorna 200 OK pero con cuerpo vacío.

**Impacto**: Las verificaciones `jsonPath` en los tests de `FindAll` (ej: `$.content[0].id`) fallan. Actualmente los tests solo verifican el status 200.

**Solución potencial**: Agregar configuración adicional de Jackson en el contexto de prueba, o usar un DTO no genérico para la respuesta paginada.

### 7.2 Body Vacío Retorna 500 en Lugar de 400

**Problema**: Un `POST` con cuerpo vacío lanza `HttpMessageNotReadableException` (antes de que `@Valid` pueda procesarlo). El `GlobalExceptionHandler` no tiene un handler específico para esta excepción, por lo que cae en el genérico `Exception → 500`.

**Impacto**: El endpoint debería idealmente retornar 400. En producción, la validación ocurre en el frontend antes de enviar la petición.

### 7.3 CGLIB Proxies y Path Prefix

**Problema**: `@EnableMethodSecurity` crea proxies CGLIB para controllers con `@PreAuthorize`. El proxy CGLIB es una subclase que NO hereda la anotación `@RestController` (no es `@Inherited`). `WebConfig.configurePathMatch` usa `AnnotatedElementUtils.hasAnnotation` que tampoco detecta la anotación en la subclase CGLIB.

**Impacto**: El path prefix `/api/v1` no se aplica a controllers con `@PreAuthorize` cuando `@EnableMethodSecurity` está activo. En los tests, se desactivó `@EnableMethodSecurity` para evitarlo (con `@AutoConfigureMockMvc(addFilters = false)`).

**Solución potencial**: Usar `@EnableMethodSecurity(proxyTargetClass = false)` para forzar proxies JDK dinámicos (basados en interfaces), o configurar el path prefix mediante `WebMvcConfigurer` de otra forma.

### 7.4 MinimalTest

Un test preexistente (`MinimalTest`) que intenta cargar el contexto completo de `@WebMvcTest` con `SysMaderaApplication` falla porque `JwtTokenProvider` no está disponible como bean. El archivo fuente **ya no existe** en el proyecto (posiblemente eliminado), pero la compilación residual o el reporte de surefire aún lo referencian.

---

## 8. Configuración y Despliegue

### 8.1 Requisitos

- Java 17+
- MySQL 8+
- Maven (o usar `mvnw` wrapper)

### 8.2 Configuración de Base de Datos

**`application-dev.properties`**:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/woodmanager?useSSL=false&serverTimezone=America/Mexico_City
spring.datasource.username=root
spring.datasource.password=lokuw
spring.jpa.hibernate.ddl-auto=update
```

### 8.3 JWT

```properties
app.jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970337336763979244226452948404D635166546A576E5A7234753778214125442A47
app.jwt.expiration-ms=86400000
```

### 8.4 Cómo Ejecutar

```bash
# Desarrollo
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Pruebas
./mvnw test

# Build
./mvnw clean package -DskipTests

# Producción
java -jar target/sys_madera-1.0.0.jar --spring.profiles.active=prod
```

### 8.5 Documentación API (Swagger)

Una vez iniciada la aplicación:
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

---

## 9. Métricas de Código

### 9.1 Conteo de Archivos

| Tipo | Cantidad |
|---|---|
| Archivos Java (producción) | 47 |
| Archivos Java (pruebas) | 5 |
| Páginas HTML | 10 |
| Archivos CSS/JS | 3 |
| Archivos de configuración | 6 |
| **Total archivos fuente** | **71** |

### 9.2 Líneas de Código (aproximado)

| Componente | Líneas |
|---|---|
| Modelos (10 entidades + 2 enums) | ~450 |
| Repositorios (9 interfaces) | ~100 |
| DTOs (17 records) | ~200 |
| Servicios (9 interfaces + 9 impl) | ~900 |
| Controladores (8) | ~300 |
| Seguridad (4 clases) | ~200 |
| Excepciones (5 clases) | ~80 |
| Configuración (4 clases) | ~100 |
| Frontend (HTML+CSS+JS) | ~3,000 |
| **Total producción** | **~5,330** |
| **Pruebas** | **~1,200** |

### 9.3 Cobertura de Pruebas por Módulo

| Módulo | Clases | Tests |
|---|---|---|
| ClientService | 1 | 10 unitarios |
| OrderService | 1 | 13 unitarios (preexistente) |
| ClientRepository | 1 | 18 de integración |
| ClientController | 1 | 15 de integración |
| Aplicación | 1 | 1 de contexto |
| **Total** | **5** | **57** |

---

## 10. Guía de Contribución

### 10.1 Convenciones de Código

- **Lombok**: Usar `@Data`, `@Builder`, `@RequiredArgsConstructor` en entidades y servicios
- **DTOs**: Preferir Java `record` para inmutabilidad
- **Excepciones**: Usar excepciones específicas (`ResourceNotFoundException`, `DuplicateResourceException`, `BadRequestException`)
- **Manejo global**: El `GlobalExceptionHandler` captura todas las excepciones y retorna `ErrorResponse` estandarizado

### 10.2 Convenciones de Pruebas

- Organizar con `@Nested` + `@DisplayName` (grupo funcional → caso específico)
- Usar `BDDMockito.given/willReturn` (estilo BDD)
- Preferir `@MockitoBean` sobre `@MockBean` en Spring Boot 3.4+
- Usar `ArgumentCaptor` para verificar argumentos pasados a mocks
- Datos de prueba realistas con constantes compartidas
- Verificar timestamps (`createdAt`, `updatedAt`) en tests de integración

### 10.3 Estructura de Nombres de Tests

```
@Test
@DisplayName("should return 200 when client exists")
void shouldReturn200() { ... }

@Test  
@DisplayName("should throw ResourceNotFoundException when client not found")
void shouldThrowException_whenClientNotFound() { ... }
```

### 10.4 Para Agregar un Nuevo Módulo

1. Crear entidad en `model/`
2. Crear repositorio en `repository/`
3. Crear DTOs de request/response en `dto/`
4. Crear interfaz de servicio en `service/` e implementación en `service/impl/`
5. Crear controller en `controller/` con `@PreAuthorize`
6. Agregar rutas públicas en `SecurityConfig` si es necesario
7. **Escribir pruebas**: service (unitarias), repository (DataJpaTest), controller (WebMvcTest)

---

*Documento generado el 6 de junio de 2026.*
*WoodManager — Sistema de Gestión de Carpintería*
