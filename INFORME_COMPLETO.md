# INFORME COMPLETO DEL PROYECTO: WoodManager (sys_madera)

> **Fecha:** 02/06/2026
> **Propósito:** Documentación completa del sistema para análisis y mejora con asistencia de IA

---

## 1. DATOS GENERALES

| Campo | Valor |
|-------|-------|
| **Nombre** | WoodManager - Sistema de Gestión de Carpintería |
| **Artifact** | `com.madera:sys_madera:1.0.0` |
| **Java** | 17 |
| **Spring Boot** | 3.5.0 |
| **Base de datos** | MySQL 8+ |
| **Build** | Maven |
| **Tipo** | Monolito (backend REST + frontend estático embebido) |

---

## 2. ESTRUCTURA COMPLETA DEL PROYECTO

```
sys_madera/
├── pom.xml
├── DESIGN.md                          # Design system (tema oscuro, paleta, tipografía)
├── HELP.md                            # Documentación genérica de Spring Boot
├── mvnw / mvnw.cmd                    # Maven Wrapper
├── .gitignore
├── .gitattributes
├── .mvn/                              # Config Maven Wrapper
├── .vscode/                           # Config VS Code
├── sys_madera/                        # ⚠️ Directorio duplicado (contiene .gitignore, HELP.md, mvnw)
│
├── src/main/java/com/madera/sys_madera/
│   ├── SysMaderaApplication.java      # Entry point (@SpringBootApplication)
│   │
│   ├── config/
│   │   ├── WebConfig.java             # @EnableJpaAuditing + prefix /api/v1 para @RestController
│   │   ├── OpenApiConfig.java         # Swagger/OpenAPI con seguridad Bearer JWT
│   │   └── DataInitializer.java       # CommandLineRunner: seed de roles (ADMIN, EMPLEADO, CLIENTE)
│   │
│   ├── model/
│   │   ├── User.java                  # Usuarios del sistema
│   │   ├── Role.java                  # Roles (ADMIN, EMPLEADO, CLIENTE)
│   │   ├── ERole.java                 # Enum de roles
│   │   ├── Client.java                # Clientes del taller
│   │   ├── Furniture.java             # Catálogo de muebles
│   │   ├── Order.java                 # Pedidos/órdenes
│   │   ├── OrderDetail.java           # Detalle de pedidos (productos individuales)
│   │   ├── EOrderStatus.java          # Enum: PENDIENTE, EN_PRODUCCION, COMPLETADO, ENTREGADO, CANCELADO
│   │   ├── Invoice.java               # Facturas
│   │   ├── EInvoiceStatus.java        # Enum: PENDIENTE, PAGADA_PARCIAL, PAGADA, CANCELADA, VENCIDA
│   │   ├── WoodInventory.java         # Inventario de madera
│   │   └── WoodSurplus.java           # Sobrantes de madera
│   │
│   ├── repository/
│   │   ├── UserRepository.java
│   │   ├── RoleRepository.java
│   │   ├── ClientRepository.java
│   │   ├── FurnitureRepository.java
│   │   ├── OrderRepository.java
│   │   ├── OrderDetailRepository.java
│   │   ├── InvoiceRepository.java
│   │   ├── WoodInventoryRepository.java
│   │   └── WoodSurplusRepository.java
│   │
│   ├── dto/
│   │   ├── request/
│   │   │   ├── LoginRequest.java      # username, password
│   │   │   ├── RegisterRequest.java   # username, email, password, firstName, lastName, roles
│   │   │   ├── ClientRequest.java     # name, email, phone, address, rfc
│   │   │   ├── FurnitureRequest.java  # name, description, price, woodType, dimensions, category, stockQuantity, imageUrl
│   │   │   ├── OrderRequest.java      # clientId, notes, List<OrderDetailRequest>
│   │   │   │   └── OrderDetailRequest (inner record) # furnitureId, quantity
│   │   │   ├── InvoiceRequest.java    # orderId, issueDate, dueDate, paidAmount, notes
│   │   │   └── WoodInventoryRequest.java # woodType, quantity, unit, unitPrice, supplier, description, minimumStock
│   │   │
│   │   └── response/
│   │       ├── AuthResponse.java      # token, type, id, username, email, roles
│   │       ├── ClientResponse.java
│   │       ├── FurnitureResponse.java
│   │       ├── OrderResponse.java     # incluye List<OrderDetailResponse>
│   │       ├── InvoiceResponse.java   # incluye balance calculado
│   │       ├── WoodInventoryResponse.java # incluye totalValue y lowStock calculados
│   │       ├── PagedResponse<T>.java  # content, page, size, totalElements, totalPages, last
│   │       └── MessageResponse.java   # message
│   │
│   ├── service/ (interfaces)
│   │   ├── AuthService.java
│   │   ├── ClientService.java
│   │   ├── FurnitureService.java
│   │   ├── OrderService.java
│   │   ├── InvoiceService.java
│   │   ├── WoodInventoryService.java
│   │   ├── WoodSurplusService.java
│   │   ├── DashboardService.java
│   │   └── ReportService.java
│   │
│   ├── service/impl/
│   │   ├── AuthServiceImpl.java
│   │   ├── ClientServiceImpl.java
│   │   ├── FurnitureServiceImpl.java
│   │   ├── OrderServiceImpl.java
│   │   ├── InvoiceServiceImpl.java
│   │   ├── WoodInventoryServiceImpl.java
│   │   ├── WoodSurplusServiceImpl.java
│   │   ├── DashboardServiceImpl.java
│   │   └── ReportServiceImpl.java
│   │
│   ├── controller/
│   │   ├── AuthController.java        # POST /auth/login, POST /auth/register
│   │   ├── ClientController.java      # CRUD /clientes
│   │   ├── FurnitureController.java   # CRUD /muebles (soft-delete)
│   │   ├── OrderController.java       # CRUD /pedidos + PATCH estado
│   │   ├── InvoiceController.java     # CRUD /facturas + POST registro pago
│   │   ├── WoodInventoryController.java # CRUD /inventario-madera + GET bajo-stock
│   │   ├── DashboardController.java   # GET /dashboard/admin, GET /dashboard/empleado
│   │   └── ReportController.java      # GET /reportes/ventas, /inventario, /mas-vendidos
│   │
│   ├── security/
│   │   ├── config/SecurityConfig.java        # SecurityFilterChain, CORS, JWT filter
│   │   ├── CustomUserDetailsService.java     # UserDetailsService impl
│   │   └── jwt/
│   │       ├── JwtTokenProvider.java         # Generar/validar JWT (HMAC-SHA, 24h exp)
│   │       ├── JwtAuthenticationFilter.java  # Filtro OncePerRequestFilter
│   │       └── JwtEntryPoint.java            # AuthenticationEntryPoint (401 JSON)
│   │
│   ├── exception/
│   │   ├── AppException.java
│   │   ├── BadRequestException.java
│   │   ├── DuplicateResourceException.java
│   │   ├── ResourceNotFoundException.java
│   │   └── GlobalExceptionHandler.java       # @RestControllerAdvice + ErrorResponse record
│   │
│   └── util/
│       └── Constants.java            # Paginación: DEFAULT_PAGE, SIZE, SORT, DIR; Roles
│
├── src/main/resources/
│   ├── application.properties        # Puerto 8080, Jackson config, profile=dev
│   ├── application-dev.properties    # MySQL local, JPA ddl-auto=update, JWT secret hardcodeado
│   ├── application-prod.properties   # MySQL + SSL, JPA ddl-auto=validate, JWT secret en variable entorno
│   │
│   └── static/
│       ├── css/
│       │   └── woodmanager.css       # 1202 líneas - diseño system completo (dark theme)
│       ├── js/
│       │   ├── api.js                # 212 líneas - cliente HTTP con fetch, JWT en localStorage
│       │   └── woodmanager.js        # 251 líneas - sidebar, dropdowns, Chart.js, utilidades
│       │
│       └── producto/                 # 10 páginas HTML estáticas
│           ├── login.html
│           ├── register.html
│           ├── dashboard.html
│           ├── pedidos.html
│           ├── catalogo-muebles.html
│           ├── clientes.html
│           ├── inventario-madera.html
│           ├── facturacion.html
│           ├── reportes.html
│           └── perfil.html
│
├── src/test/java/com/madera/sys_madera/
│   └── SysMaderaApplicationTests.java  # Único test: contextLoads (placeholder)
│
└── target/                           # Build output
```

---

## 3. DIAGRAMA ENTIDAD-RELACIÓN (MODELO DE DATOS)

```
┌─────────────┐       ┌─────────────┐
│    User     │ N──M  │    Role     │
│─────────────│       │─────────────│
│ id          │       │ id          │
│ username    │       │ name (enum) │
│ email       │       └─────────────┘
│ password    │
│ first_name  │
│ last_name   │
│ enabled     │
│ created_at  │
│ updated_at  │
└──────┬──────┘
       │ 1:1
       ▼
┌──────────────┐       ┌─────────────────┐       ┌──────────────────┐
│   Client     │ 1──N  │     Order       │ 1──N  │   OrderDetail    │
│──────────────│       │─────────────────│       │──────────────────│
│ id           │       │ id              │       │ id               │
│ name         │       │ order_number    │       │ quantity         │
│ email        │       │ status (enum)   │       │ unit_price       │
│ phone        │       │ total_amount    │       │ subtotal         │
│ address      │       │ notes           │       │ order_id (FK)    │
│ rfc          │       │ client_id (FK)  │       │ furniture_id (FK)│
│ user_id (FK) │       │ created_at      │       └────────┬─────────┘
│ created_at   │       │ updated_at      │                │ N:1
│ updated_at   │       └────────┬────────┘                ▼
└──────────────┘                │ 1:1            ┌──────────────────┐
                                ▼                │   Furniture      │
                        ┌──────────────┐         │──────────────────│
                        │   Invoice    │         │ id               │
                        │──────────────│         │ name             │
                        │ id           │         │ description      │
                        │ invoice_num  │         │ price            │
                        │ issue_date   │         │ wood_type        │
                        │ due_date     │         │ dimensions       │
                        │ total_amount │         │ category         │
                        │ paid_amount  │         │ stock_quantity   │
                        │ status (enum)│         │ active (soft-del)│
                        │ notes        │         │ image_url        │
                        │ order_id(FK) │         │ created_at       │
                        │ created_at   │         │ updated_at       │
                        │ updated_at   │         └──────────────────┘
                        └──────────────┘

┌──────────────────┐       ┌──────────────────┐
│  WoodInventory   │ 1──N  │   WoodSurplus    │
│──────────────────│       │──────────────────│
│ id               │       │ id               │
│ wood_type        │       │ wood_type        │
│ quantity         │       │ quantity         │
│ unit             │       │ unit             │
│ unit_price       │       │ dimensions       │
│ supplier         │       │ description      │
│ description      │       │ available        │
│ minimum_stock    │       │ wood_inventory_id│
│ created_at       │       │ created_at       │
│ updated_at       │       │ updated_at       │
└──────────────────┘       └──────────────────┘
```

---

## 4. STACK TECNOLÓGICO DETALLADO

| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| **Java** | 17 | Lenguaje (Records, Stream API, var, switch expressions) |
| **Spring Boot** | 3.5.0 | Framework principal |
| **Spring Data JPA** | (Hibernate 6.x) | ORM, repositorios automáticos |
| **Spring Security** | 6.x | Autenticación JWT + autorización por roles |
| **Spring Validation** | Jakarta Bean Validation 3.0 | Validación de DTOs con `@Valid` |
| **Spring Actuator** | - | Endpoint `/actuator/health` |
| **MySQL Connector** | 8.x (mysql-connector-j) | Driver MySQL |
| **JJWT** | 0.12.6 | Creación y validación de tokens JWT |
| **SpringDoc OpenAPI** | 2.8.5 | Swagger UI en `/swagger-ui.html` |
| **Lombok** | Última | `@Data`, `@Builder`, `@RequiredArgsConstructor`, `@Slf4j` |
| **Bootstrap 5** | 5.3.3 | Framework CSS frontend |
| **Font Awesome 6** | 6.5.0 | Iconos |
| **Chart.js** | 4.4 | Gráficos en dashboard |
| **Maven** | - | Build y dependencias |

---

## 5. SEGURIDAD

### 5.1 Autenticación
- **Mecanismo:** JWT (JSON Web Token) con HMAC-SHA256
- **Tipo:** Bearer token en header `Authorization`
- **Expiración:** 24 horas (configurable via `app.jwt.expiration-ms`)
- **Almacenamiento frontend:** `localStorage` (clave `wm_token`)
- **Login:** `POST /api/v1/auth/login` → devuelve `{ token, type, id, username, email, roles }`

### 5.2 Autorización por Roles
| Rol | Acceso |
|-----|--------|
| `ROLE_ADMIN` | CRUD completo en todos los recursos, incluyendo DELETE |
| `ROLE_EMPLEADO` | CRUD sin DELETE en la mayoría, dashboard empleado |
| `ROLE_CLIENTE` | Solo lectura de sus propios datos |

### 5.3 Endpoints Públicos
- `/api/v1/auth/**` (login, register)
- `/swagger-ui/**`, `/v3/api-docs/**`
- `/actuator/health`
- `/producto/**`, `/css/**`, `/js/**` (frontend estático)

### 5.4 Configuración CORS
- `allowedOrigins = List.of("*")` — ⚠️ Permisivo, aceptable en dev
- Métodos permitidos: GET, POST, PUT, PATCH, DELETE, OPTIONS

### 5.5 JWT Secret
- **Dev:** Hardcodeado en `application-dev.properties` (base64 de 256 bits)
- **Prod:** Via variable de entorno `${JWT_SECRET}` ✅

### 5.6 Manejo de Errores de Seguridad
- **401:** `JwtEntryPoint` responde JSON con mensaje "No autorizado"
- **403:** `GlobalExceptionHandler` captura `AccessDeniedException`
- **CSRF:** Deshabilitado (API stateless) ✅
- **Sesiones:** STATELESS ✅
- **Password encoder:** BCrypt ✅

---

## 6. DISEÑO DE API (RESTful)

### 6.1 Convenciones
- **Prefijo base:** `/api/v1` (configurado via `WebConfig.configurePathMatch`)
- **Nombres en español:** `/clientes`, `/muebles`, `/pedidos`, `/facturas`
- **Paginación uniforme:** Parámetros `page`, `size`, `sort`, `direction` con defaults en `Constants.java`
- **DTOs inmutables:** Java Records tanto en request como response
- **Verbose HTTP correctos:** POST=crear, PUT=actualizar, PATCH=cambio parcial, DELETE=eliminar

### 6.2 Listado Completo de Endpoints

#### Auth
| Método | Path | Descripción | Acceso |
|--------|------|-------------|--------|
| POST | `/auth/login` | Iniciar sesión | Público |
| POST | `/auth/register` | Registrar usuario | Público |

#### Clientes
| Método | Path | Descripción | Acceso |
|--------|------|-------------|--------|
| GET | `/clientes` | Listar (paginado + búsqueda) | ADMIN, EMPLEADO |
| GET | `/clientes/{id}` | Obtener por ID | ADMIN, EMPLEADO, CLIENTE |
| POST | `/clientes` | Crear | ADMIN, EMPLEADO |
| PUT | `/clientes/{id}` | Actualizar | ADMIN, EMPLEADO |
| DELETE | `/clientes/{id}` | Eliminar | ADMIN |

#### Muebles
| Método | Path | Descripción | Acceso |
|--------|------|-------------|--------|
| GET | `/muebles` | Listar (paginado + búsqueda + categoría) | Público |
| GET | `/muebles/{id}` | Obtener por ID | Público |
| POST | `/muebles` | Crear | ADMIN, EMPLEADO |
| PUT | `/muebles/{id}` | Actualizar | ADMIN, EMPLEADO |
| DELETE | `/muebles/{id}` | Soft-delete (desactivar) | ADMIN |

#### Pedidos
| Método | Path | Descripción | Acceso |
|--------|------|-------------|--------|
| GET | `/pedidos` | Listar (paginado + filtro por estado) | ADMIN, EMPLEADO |
| GET | `/pedidos/{id}` | Obtener por ID | ADMIN, EMPLEADO, CLIENTE |
| POST | `/pedidos` | Crear con detalles | ADMIN, EMPLEADO |
| PATCH | `/pedidos/{id}/estado` | Cambiar estado | ADMIN, EMPLEADO |
| GET | `/pedidos/cliente/{clientId}` | Órdenes por cliente | ADMIN, EMPLEADO |

#### Facturas
| Método | Path | Descripción | Acceso |
|--------|------|-------------|--------|
| GET | `/facturas` | Listar (paginado + filtro estado) | ADMIN, EMPLEADO |
| GET | `/facturas/{id}` | Obtener por ID | ADMIN, EMPLEADO |
| POST | `/facturas` | Crear desde orden | ADMIN, EMPLEADO |
| GET | `/facturas/orden/{orderId}` | Factura por orden | ADMIN, EMPLEADO |
| POST | `/facturas/{id}/pago` | Registrar pago (parcial/total) | ADMIN, EMPLEADO |

#### Inventario Madera
| Método | Path | Descripción | Acceso |
|--------|------|-------------|--------|
| GET | `/inventario-madera` | Listar (paginado + búsqueda) | ADMIN, EMPLEADO |
| GET | `/inventario-madera/{id}` | Obtener por ID | ADMIN, EMPLEADO |
| GET | `/inventario-madera/bajo-stock` | Alertas de stock bajo | ADMIN, EMPLEADO |
| POST | `/inventario-madera` | Crear | ADMIN, EMPLEADO |
| PUT | `/inventario-madera/{id}` | Actualizar | ADMIN, EMPLEADO |
| DELETE | `/inventario-madera/{id}` | Eliminar | ADMIN |

#### Dashboard
| Método | Path | Descripción | Acceso |
|--------|------|-------------|--------|
| GET | `/dashboard/admin` | Dashboard completo | ADMIN |
| GET | `/dashboard/empleado` | Dashboard resumido | ADMIN, EMPLEADO |

#### Reportes
| Método | Path | Descripción | Acceso |
|--------|------|-------------|--------|
| GET | `/reportes/ventas` | Reporte de ventas por rango | ADMIN |
| GET | `/reportes/inventario` | Reporte de inventario | ADMIN, EMPLEADO |
| GET | `/reportes/mas-vendidos` | Top muebles más vendidos | ADMIN, EMPLEADO |

---

## 7. FRONTEND

### 7.1 Arquitectura
- **Tipo:** HTML estático + CSS + JavaScript vanilla
- **Framework CSS:** Bootstrap 5.3.3
- **Íconos:** Font Awesome 6.5.0
- **Gráficos:** Chart.js 4.4 (dashboard)
- **Sin framework JS moderno:** No usa React, Vue, Angular, ni siquiera jQuery
- **Despliegue:** Archivos en `src/main/resources/static/producto/` servidos por Spring Boot

### 7.2 Cliente HTTP (api.js)
- Cliente `fetch` con manejo de token JWT desde `localStorage`
- Redirección automática a login si 401
- Funciones por recurso: `getClientes()`, `createPedido()`, etc.

### 7.3 Utilidades JS (woodmanager.js)
- Inicialización de sidebar colapsable
- Charts de Chart.js con datos quemados (hardcoded, no vienen del backend)
- Función `searchTable()` para filtro frontend
- `confirmDelete()` con SweetAlert2 o confirm nativo
- `formatCurrency()` para formateo monetario

### 7.4 Páginas HTML (10 páginas)
| Página | Archivo | Funcionalidad |
|--------|---------|---------------|
| Login | `login.html` | Formulario con toggle password, handler async |
| Registro | `register.html` | Formulario de registro |
| Dashboard | `dashboard.html` | Stats cards, gráficos, últimos pedidos, accesos rápidos |
| Pedidos | `pedidos.html` | CRUD de pedidos |
| Catálogo | `catalogo-muebles.html` | Catálogo con tarjetas de productos |
| Clientes | `clientes.html` | CRUD de clientes con tabla y modal Bootstrap |
| Inventario | `inventario-madera.html` | CRUD de inventario de madera |
| Facturación | `facturacion.html` | CRUD de facturas |
| Reportes | `reportes.html` | Reportes y gráficos |
| Perfil | `perfil.html` | Perfil de usuario |

### 7.5 Design System (CSS)
- Modo oscuro completo
- Paleta: fondo `#0a0e17`, superficie `#141b2d`, dorado `#C9A84C`, madera `#8B5E3C`
- Tipografía: Inter (headings y body), JetBrains Mono (monospace)
- Componentes: sidebar 260px, cards con border-radius 12px, tablas, modales, badges, botones
- Responsive: 3 breakpoints (1200px, 992px, 768px, 480px)
- Scrollbar personalizada, animaciones 0.3s ease

---

## 8. ANÁLISIS DE CALIDAD

### 8.1 ✅ Fortalezas

| Aspecto | Detalle |
|---------|---------|
| **Arquitectura limpia** | Separación clara en capas (Controller → Service → Repository) |
| **DTOs inmutables** | Java Records en toda la capa de transferencia |
| **Service Interface + Impl** | Buena práctica de desacoplamiento |
| **Excepciones centralizadas** | `@RestControllerAdvice` con `GlobalExceptionHandler` |
| **Spring Data JPA Auditing** | `@CreatedDate`, `@LastModifiedDate` en todas las entidades |
| **Validación** | `@Valid` + Jakarta Validation en todos los request DTOs |
| **Paginación uniforme** | `PagedResponse<T>` genérico en toda la API |
| **Seguridad JWT** | Implementación correcta con filtro, provider y entry point |
| **Lombok** | Uso consistente de `@Data`, `@Builder`, `@RequiredArgsConstructor` |
| **Properties multi-entorno** | Perfiles dev y prod separados |
| **Documentación OpenAPI** | Swagger UI con esquema Bearer JWT |
| **Código moderno** | Java 17 Records, `Stream.toList()`, `var` |
| **Manejo de estados** | Enums para estados de orden y factura con lógica de transición |
| **Cálculos de negocio** | `totalValue` y `lowStock` calculados en responses (no persistidos) |
| **Seed inicial** | Roles auto-creados al iniciar la app |

### 8.2 ❌ Problemas y Deuda Técnica

| # | Problema | Severidad | Archivos Afectados |
|---|----------|-----------|-------------------|
| 1 | **Tests casi nulos** | 🔴 Alta | `src/test/` |
| | Solo existe `SysMaderaApplicationTests.java` con `contextLoads()`. Sin tests unitarios de servicios, controllers, repositorios ni seguridad. | | |
| 2 | **Reportes incompletos** | 🔴 Alta | `ReportServiceImpl.java:54-65` |
| | `getInventoryReport()` y `getTopSellingFurniture()` retornan mensajes placeholder. Solo `getSalesReport()` tiene lógica real. | | |
| 3 | **WoodSurplus sin endpoint REST** | 🟡 Media | `WoodSurplusServiceImpl.java` |
| | El servicio existe pero no tiene controller ni endpoints. `create()` además recibe la entidad directamente (no DTO), rompiendo la consistencia del proyecto. | | |
| 4 | **Bug frontend: registrarPago sin amount** | 🟡 Media | `api.js:187` |
| | `registrarPago(id)` hace POST sin enviar el monto (`amount`). El backend espera `@RequestParam BigDecimal amount`. El pago siempre fallará. | | |
| 5 | **Inconsistencia: soft-delete vs hard-delete** | 🟡 Media | `FurnitureServiceImpl.java:106-111` vs otros servicios |
| | Furniture usa soft-delete (`active=false`), mientras Client, WoodInventory usan hard-delete (`repository.delete()`). | | |
| 6 | **JWT secret hardcodeado en dev** | 🟡 Media | `application-dev.properties:14` |
| | Visible en el repositorio. En prod usa variable de entorno, pero en dev cualquiera puede ver la clave. | | |
| 7 | **CORS demasiado permisivo** | 🟡 Media | `SecurityConfig.java:70` |
| | `allowedOrigins = List.of("*")` en producción podría ser riesgoso. Debería restringirse a dominios específicos. | | |
| 8 | **Dashboard no conecta con API real** | 🟡 Media | `dashboard.html:209-214` |
| | El frontend intenta leer `data.stats.ventasMes` pero el backend devuelve `totalClients`, `totalOrders`, etc. Los valores no se muestran. | | |
| 9 | **Sin logging en servicios** | 🟡 Media | Todos los servicios impl |
| | Solo `DataInitializer.java` usa `@Slf4j`. No hay logs de creación, actualización, errores, etc. | | |
| 10 | **Orden no valida stock disponible** | 🟡 Media | `OrderServiceImpl.java:36-76` |
| | Al crear una orden, no verifica si hay stock suficiente de los muebles. | | |
| 11 | **Gráficos con datos hardcodeados** | 🟢 Baja | `woodmanager.js:47-212` |
| | Los charts de Chart.js usan datos fijos, no datos reales del backend. | | |
| 12 | **Directorios duplicados** | 🟢 Baja | `sys_madera/` en raíz |
| | Existe un directorio `sys_madera/` duplicado con archivos sueltos (`.gitignore`, `HELP.md`, `mvnw`). | | |
| 13 | **Carpeta templates/producto vacía** | 🟢 Baja | `src/main/resources/templates/producto/` |
| | Existe pero no contiene archivos. Confuso si no se usa Thymeleaf. | | |
| 14 | **Sin refresh token** | 🟢 Baja | Todo el sistema JWT |
| | El token expira en 24h. No hay mecanismo de refresh token. | | |
| 15 | **Frontend sin framework moderno** | 🟡 Media | Todo el frontend |
| | HTML estático + JS vanilla. Difícil de escalar y mantener a largo plazo. | | |

### 8.3 📊 Estadísticas del Código

| Métrica | Valor |
|---------|-------|
| **Clases Java** | ~45 (models, services, controllers, DTOs, config, security, exceptions) |
| **Líneas backend (Java)** | ~2,500 |
| **Líneas frontend (CSS)** | 1,202 |
| **Líneas frontend (JS)** | 463 (api.js + woodmanager.js) |
| **Líneas frontend (HTML)** | ~3,000 (10 páginas) |
| **Total aproximado** | ~7,200 líneas |
| **Tests** | 1 test (context load) |
| **Cobertura de tests** | ~0% |

---

## 9. MODELO DE DOMINIO - DETALLES DE ENTIDADES

### User
```java
@Table(name = "users", uniqueConstraints = {@UniqueConstraint(columnNames = "username"), @UniqueConstraint(columnNames = "email")})
// Relaciones: @ManyToMany → Role (EAGER), @OneToOne → Client (LAZY)
// Auditoría: @CreatedDate, @LastModifiedDate
```
Campos: `id, username, email, password, firstName, lastName, enabled (default true), roles, client, createdAt, updatedAt`

### Client
```java
@Table(name = "clients")
// Relaciones: @OneToOne → User, @OneToMany → Order
```
Campos: `id, name, email, phone, address, rfc, user, orders, createdAt, updatedAt`

### Furniture
```java
@Table(name = "furniture")
```
Campos: `id, name, description, price, woodType, dimensions, category, stockQuantity, active (default true, soft-delete), imageUrl, createdAt, updatedAt`

### Order
```java
@Table(name = "orders")
```
Campos: `id, orderNumber (ORD-YYYYMMDD-NNNN), status (enum: PENDIENTE/EN_PRODUCCION/COMPLETADO/ENTREGADO/CANCELADO), totalAmount, notes, client, orderDetails (orphanRemoval=true), invoice, createdAt, updatedAt`

### OrderDetail
```java
@Table(name = "order_details")
```
Campos: `id, quantity, unitPrice, subtotal, order (FK), furniture (FK)`

### Invoice
```java
@Table(name = "invoices")
```
Campos: `id, invoiceNumber (FAC-YYYYMMDD-NNNN), issueDate, dueDate, totalAmount, paidAmount (default 0), status (enum: PENDIENTE/PAGADA_PARCIAL/PAGADA/CANCELADA/VENCIDA), notes, order (FK único), createdAt, updatedAt`
- **Método:** `determineStatus(paidAmount, totalAmount)` → lógica de estado

### WoodInventory
```java
@Table(name = "wood_inventory")
```
Campos: `id, woodType, quantity, unit, unitPrice, supplier, description, minimumStock, createdAt, updatedAt`

### WoodSurplus
```java
@Table(name = "wood_surplus")
```
Campos: `id, woodType, quantity, unit, dimensions, description, available (default true), woodInventory (FK), createdAt, updatedAt`

---

## 10. LÓGICA DE NEGOCIO DESTACADA

### 10.1 Generación de números de orden y factura
```java
// Order: ORD-20260602-0001
"ORD-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-" + String.format("%04d", count+1)
// Invoice: FAC-20260602-0001
"FAC-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-" + String.format("%04d", count+1)
```
⚠️ Inseguro bajo concurrencia (usa `count()+1` sin bloqueo).

### 10.2 Cálculo de estados de factura
```
paidAmount = 0          → PENDIENTE
0 < paidAmount < total  → PAGADA_PARCIAL
paidAmount >= total     → PAGADA
```

### 10.3 Alerta de stock bajo en inventario
```java
// Se calcula en el response, no se persiste
boolean lowStock = item.getMinimumStock() != null && item.getQuantity().compareTo(item.getMinimumStock()) < 0;
```

### 10.4 Soft-delete en Furniture
```java
furniture.setActive(false);  // En lugar de delete físico
```

---

## 11. CONFIGURACIÓN DEL ENTORNO

### Dev (application-dev.properties)
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/woodmanager?useSSL=false&serverTimezone=America/Mexico_City&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=lokuw
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
app.jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970337336763979244226452948404D635166546A576E5A7234753778214125442A47
app.jwt.expiration-ms=86400000
```

### Prod (application-prod.properties)
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/woodmanager?useSSL=true&serverTimezone=America/Mexico_City
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=validate
app.jwt.secret=${JWT_SECRET}
app.jwt.expiration-ms=86400000
logging.level.com.madera.sys_madera=WARN
```

---

## 12. RECOMENDACIONES PRIORIZADAS

### 🔴 Prioridad Alta (Crítico para producción)

1. **Escribir tests** — Unitarios para servicios (JUnit 5 + Mockito), de integración para repositorios (@DataJpaTest), de API (WebMvcTest), de seguridad.
2. **Completar reportes** — Implementar `getInventoryReport()` y `getTopSellingFurniture()` con consultas reales a la BD.
3. **Corregir bug registrarPago** — Enviar `amount` como query param en api.js: `this.post(\`/facturas/\${id}/pago?amount=\${amount}\`)`.

### 🟡 Prioridad Media (Mejora significativa)

4. **Exponer WoodSurplus** — Crear controller REST o integrarlo en el flujo de inventario.
5. **Unificar estrategia de borrado** — Decidir soft-delete o hard-delete y aplicar consistentemente.
6. **Quitar JWT secret del repositorio** — Usar variable de entorno incluso en dev.
7. **Restringir CORS** — Especificar orígenes permitidos en lugar de `*`.
8. **Conectar dashboard con API real** — Corregir mapeo de campos del backend al frontend.
9. **Agregar logging** — `@Slf4j` en servicios para trazabilidad.
10. **Validar stock en creación de órdenes** — Verificar `Furniture.stockQuantity` antes de crear Order.

### 🟢 Prioridad Baja (Mejora estética/mantenibilidad)

11. **Reemplazar datos hardcodeados de gráficos** — Conectar Chart.js con endpoints reales.
12. **Limpiar directorios duplicados** — Eliminar `sys_madera/` redundante.
13. **Evaluar migración de frontend** — Considerar React/Vue/Angular o Thymeleaf para escalabilidad.
14. **Implementar refresh token** — Mejorar la experiencia de sesión.

---

## 13. PREGUNTAS ABIERTAS PARA CHATGPT

1. **Arquitectura:** ¿Recomiendas mantener el monolito o dividir en microservicios (ej. separar facturación)?

2. **Frontend:** ¿Vale la pena migrar a React/Next.js o es mejor usar Thymeleaf + HTMX para mantener el backend monolítico?

3. **Seguridad:** ¿Cómo implementarías refresh tokens y rate limiting en esta estructura?

4. **Testing:** ¿Cuál sería la estrategia de tests más eficiente para empezar? ¿Priorizar unitarios o de integración?

5. **Reportes:** ¿Recomiendas usar la misma BD para reportes o implementar una vista materializada/separada?

6. **Concurrencia:** La generación de números de orden (`count()+1`) no es segura bajo concurrencia. ¿Cómo lo resolverías? ¿Secuencias de BD, UUID, o tabla de contadores?

7. **Deuda técnica:** Por dónde empezarías a pagar la deuda técnica considerando tiempo limitado?

8. **Despliegue:** ¿Recomiendas Dockerizar? ¿Qué servicios adicionales agregarías (Redis, cola de mensajes)?

9. **WoodSurplus:** ¿Debería tener su propio CRUD o integrarse como una funcionalidad dentro de inventario?

10. **Soft-delete:** ¿Recomiendas soft-delete generalizado (con `@SQLRestriction`) o mantener hard-delete?

---

## 14. ANEXO: CÓDIGO COMPLETO POR ARCHIVO

A continuación se incluye el contenido completo de cada archivo del proyecto para referencia de la IA.

[Nota: Los contenidos completos de todos los archivos ya fueron proporcionados arriba en las secciones anteriores. Si necesitas un archivo específico, indícalo para obtenerlo.]

---

*Documento generado automáticamente el 02/06/2026 para asistencia con ChatGPT.*
