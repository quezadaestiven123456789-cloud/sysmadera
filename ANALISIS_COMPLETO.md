# Análisis Completo del Proyecto — sys_madera (WoodManager)

---

## 1. CLASES DE PRODUCCIÓN (73 archivos)

**Spring Boot:** 3.5.0 | **Java:** 17 | **Build:** Maven

### Modelos (10 entidades)

| Entidad | Tabla | Audit | Soft Delete |
|---|---|---|---|
| `Role` | `roles` | ❌ | ❌ |
| `User` | `users` | ✅ | ❌ |
| `Client` | `clients` | ✅ | ❌ |
| `Furniture` | `furniture` | ✅ | ✅ (`active` boolean) |
| `Order` | `orders` | ✅ | ❌ |
| `OrderDetail` | `order_details` | ❌ | ❌ |
| `Invoice` | `invoices` | ✅ | ❌ |
| `WoodInventory` | `wood_inventory` | ✅ | ❌ |
| `WoodSurplus` | `wood_surplus` | ✅ | ❌ |

### Enums (3)

| Enum | Valores |
|---|---|
| `ERole` | `ROLE_ADMIN`, `ROLE_EMPLEADO`, `ROLE_CLIENTE` |
| `EOrderStatus` | `PENDIENTE`, `EN_PRODUCCION`, `COMPLETADO`, `ENTREGADO`, `CANCELADO` |
| `EInvoiceStatus` | `PENDIENTE`, `PAGADA_PARCIAL`, `PAGADA`, `CANCELADA`, `VENCIDA` |

### Repositorios (9)

| Repositorio | Métodos custom |
|---|---|
| `RoleRepository` | `findByName(ERole)` |
| `UserRepository` | `findByUsername`, `findByEmail`, `existsByUsername`, `existsByEmail` |
| `ClientRepository` | `findByEmail`, `findByRfc`, `findByUserId`, `findByNameContainingIgnoreCase`, `existsByEmail`, `existsByRfc` |
| `FurnitureRepository` | `findByNameContainingIgnoreCase`, `findByCategoryIgnoreCase`, `findByActiveTrue`, `findByStockQuantityLessThan`, `existsByName` |
| `OrderRepository` | `findByOrderNumber`, `findByClientId`, `findByStatus`, `findByStatusAndCreatedAtBetween`, `findByCreatedAtBetween`, `countByStatus` |
| `OrderDetailRepository` | `findByOrderId`, `findByFurnitureId`, `findTopSellingFurniture` (@Query) |
| `InvoiceRepository` | `findByInvoiceNumber`, `findByOrderId`, `findByStatus`, `findByStatusAndDueDateBefore`, `findByIssueDateBetween`, `countByStatus` |
| `WoodInventoryRepository` | `findByWoodTypeIgnoreCase`, `findByWoodTypeContainingIgnoreCase`, `findByQuantityLessThan`, `findBySupplierContainingIgnoreCase`, `findInventoryGroupedByType` (@Query) |
| `WoodSurplusRepository` | `findByAvailableTrue`, `findByWoodTypeContainingIgnoreCase`, `findByWoodInventoryId` |

### Servicios (9 interfaces + 9 implementaciones)

| Interface | Implementación | Métodos |
|---|---|---|
| `AuthService` | `AuthServiceImpl` | `login`, `register` |
| `ClientService` | `ClientServiceImpl` | `create`, `update`, `findById`, `findAll`, `delete` |
| `FurnitureService` | `FurnitureServiceImpl` | `create`, `update`, `findById`, `findAll`, `delete` |
| `OrderService` | `OrderServiceImpl` | `create`, `findById`, `findAll`, `findByClientId`, `updateStatus` |
| `InvoiceService` | `InvoiceServiceImpl` | `create`, `findById`, `findByOrderId`, `findAll`, `registerPayment` |
| `WoodInventoryService` | `WoodInventoryServiceImpl` | `create`, `update`, `findById`, `findAll`, `delete`, `findLowStock` |
| `WoodSurplusService` | `WoodSurplusServiceImpl` | `create`, `findAllAvailable`, `findByWoodType` |
| **`DashboardService`** | **`DashboardServiceImpl`** | **`getAdminDashboard`, `getEmployeeDashboard`** |
| **`ReportService`** | **`ReportServiceImpl`** | **`getSalesReport`, `getInventoryReport`, `getTopSellingFurniture`** |

### Controladores (8)

| Controller | Endpoints | Ruta base |
|---|---|---|
| `AuthController` | `POST /login`, `POST /register` | `/auth` |
| `ClientController` | `POST`, `PUT /{id}`, `GET /{id}`, `GET`, `DELETE /{id}` | `/clientes` |
| `FurnitureController` | `POST`, `PUT /{id}`, `GET /{id}`, `GET`, `DELETE /{id}` | `/muebles` |
| `OrderController` | `POST`, `GET /{id}`, `GET`, `GET /cliente/{clientId}`, `PATCH /{id}/estado` | `/pedidos` |
| `InvoiceController` | `POST`, `GET /{id}`, `GET /orden/{orderId}`, `GET`, `POST /{id}/pago` | `/facturas` |
| `WoodInventoryController` | `POST`, `PUT /{id}`, `GET /{id}`, `GET`, `GET /bajo-stock`, `DELETE /{id}` | `/inventario-madera` |
| **`DashboardController`** | **`GET /admin`, `GET /empleado`** | **`/dashboard`** |
| **`ReportController`** | **`GET /ventas`, `GET /inventario`, `GET /mas-vendidos`** | **`/reportes`** |

### Seguridad (4 clases)

| Clase | Métodos públicos |
|---|---|
| `SecurityConfig` | `securityFilterChain`, `corsConfigurationSource`, `passwordEncoder`, `authenticationManager` (+ privado `publicEndpoints`) |
| `CustomUserDetailsService` | `loadUserByUsername` |
| `JwtTokenProvider` | `generateToken`, `getUsernameFromToken`, `validateToken` |
| `JwtAuthenticationFilter` | `doFilterInternal` (+ privado `extractToken`) |
| `JwtEntryPoint` | `commence` |

### Exception (5 clases)

| Clase | Función |
|---|---|
| `GlobalExceptionHandler` (`@RestControllerAdvice`) | 8 handlers: ResourceNotFound → 404, BadRequest → 400, DuplicateResource → 409, BadCredentials → 401, AccessDenied → 403, MethodArgumentNotValid → 400 + errors map, AppException → status dinámico, Exception → 500 |
| `ResourceNotFoundException` | 404 con resourceName, fieldName, fieldValue |
| `BadRequestException` | 400 |
| `DuplicateResourceException` | 409 |
| `AppException` | Status dinámico |

### Config (4 clases)

| Clase | Propósito |
|---|---|
| `DataInitializer` | `CommandLineRunner` — crea roles ROLE_ADMIN, ROLE_EMPLEADO, ROLE_CLIENTE si no existen |
| `JpaConfig` | `@EnableJpaAuditing` |
| `OpenApiConfig` | Bean `OpenAPI` para Swagger/SpringDoc |
| `WebConfig` | `configurePathMatch` — trailing slash |

### DTOs (12 records)

**Request (7):** `LoginRequest`, `RegisterRequest`, `ClientRequest`, `FurnitureRequest`, `OrderRequest` (+ inner `OrderDetailRequest`), `InvoiceRequest`, `WoodInventoryRequest`

**Response (8):** `AuthResponse`, `MessageResponse`, `ClientResponse`, `FurnitureResponse`, `OrderResponse` (+ inner `OrderDetailResponse`), `InvoiceResponse`, `PagedResponse<T>`, `WoodInventoryResponse`, `TopSellingFurnitureItem`, `WoodInventoryReportItem`

---

## 2. TESTS EXISTENTES (32 archivos, ~459 tests)

### Repository Tests (8/9 — 8 archivos, ~139 tests)

| Archivo | Tests | Custom methods cubiertos |
|---|---|---|
| `RoleRepositoryTest` | 12 | `findByName` ✅ |
| `UserRepositoryTest` | 15 | `findByUsername`, `findByEmail`, `existsByUsername`, `existsByEmail`, unique constraints ✅ |
| `ClientRepositoryTest` | 18 | `findByEmail`, `findByRfc`, `existsByEmail`, `existsByRfc`, `findByNameContainingIgnoreCase`, unique constraints ✅ **Falta: `findByUserId`** |
| `FurnitureRepositoryTest` | 24 | `findByNameContainingIgnoreCase`, `findByCategoryIgnoreCase`, `findByActiveTrue`, `findByStockQuantityLessThan`, `existsByName`, soft delete ✅ |
| `OrderRepositoryTest` | 18 | `findByOrderNumber`, `findByClientId`, `findByStatus`, `findByStatusAndCreatedAtBetween`, `findByCreatedAtBetween`, `countByStatus`, unique constraint ✅ |
| `InvoiceRepositoryTest` | 17 | `findByInvoiceNumber`, `findByOrderId`, `findByStatus`, `findByStatusAndDueDateBefore`, `findByIssueDateBetween`, `countByStatus` ✅ |
| `WoodInventoryRepositoryTest` | 18 | `findByWoodTypeIgnoreCase`, `findByWoodTypeContainingIgnoreCase`, `findByQuantityLessThan`, `findBySupplierContainingIgnoreCase`, `findInventoryGroupedByType` ✅ |
| `WoodSurplusRepositoryTest` | 17 | `findByAvailableTrue`, `findByWoodTypeContainingIgnoreCase`, `findByWoodInventoryId` ✅ |

**FALTA**: `OrderDetailRepositoryTest` — 0 tests (3 métodos: `findByOrderId`, `findByFurnitureId`, `findTopSellingFurniture`)

### Service Tests (7/9 — 7 archivos, ~108 tests)

| Archivo | Tests | Cubiertos |
|---|---|---|
| `AuthServiceImplTest` | 10 | login (3), register (7) |
| `ClientServiceImplTest` | 9 | create (2), findById (2), update (3), delete (2). **Falta: `findAll`** |
| `FurnitureServiceImplTest` | 18 | create (3), update (2), findById (3), findAll (5), delete (3), soft delete (2) |
| `OrderServiceTest` | 20 | create (9), findById (2), findAll (3), findByClientId (2), updateStatus (3) |
| `InvoiceServiceImplTest` | 21 | create (7), findById (2), findByOrderId (2), findAll (3), registerPayment (7) |
| `WoodInventoryServiceImplTest` | 20 | create (4), update (5), findById (2), findAll (3), delete (2), findLowStock (3) |
| `WoodSurplusServiceImplTest` | 10 | create (4), findAllAvailable (2), findByWoodType (4) |

**FALTA**: `DashboardServiceImplTest` (0 tests), `ReportServiceImplTest` (0 tests)

### Controller Tests (6/8 — 6 archivos, ~96 tests)

| Archivo | Tests | Endpoints cubiertos | Anotación |
|---|---|---|---|
| `AuthControllerTest` | 6 | login (3), register (3) | `@WebMvcTest` + `addFilters=false` |
| `ClientControllerTest` | 17 | create (3), findById (2), findAll (2), update (1), delete (2), accessControl (7) | `@WebMvcTest` + `addFilters=false` |
| `FurnitureControllerTest` | 19 | create (3), update (3), findById (3), findAll (3), delete (2), accessControl (5) | `@WebMvcTest` + `addFilters=false` |
| `OrderControllerTest` | 16 | create (2), findById (2), findAll (2), findByClientId (1), updateStatus (2), accessControl (7) | `@WebMvcTest` + `addFilters=false` |
| `InvoiceControllerTest` | 20 | create (3), findById (2), findByOrderId (2), findAll (2), registerPayment (4), accessControl (7) | `@WebMvcTest` + `addFilters=false` |
| `WoodInventoryControllerTest` | 19 | create (3), update (2), findById (2), findAll (2), findLowStock (1), delete (2), accessControl (7) | `@WebMvcTest` + `addFilters=false` |

**FALTA**: `DashboardControllerTest`, `ReportControllerTest`

### Integration Tests (6 archivos, ~98 tests)

| Archivo | Tests | Lo que cubre |
|---|---|---|
| `AuditIntegrationTest` | 28 | 7 entidades audit: createdAt/updatedAt onCreate y onUpdate |
| `ClientIntegrationTest` | 12 | CRUD + constraints unique email/rfc |
| `FurnitureIntegrationTest` | 20 | CRUD + soft delete + stock deduction on order |
| `OrderIntegrationTest` | 11 | CRUD + stock deduction + status |
| `InvoiceIntegrationTest` | 16 | Flujo completo: Client → Order → Invoice + payment lifecycle |
| `WoodInventoryIntegrationTest` | 14 | CRUD + low stock + constraints |

### Otros Tests (3 archivos, ~18 tests)

| Archivo | Tests | Tipo |
|---|---|---|
| `GlobalExceptionHandlerTest` | 9 | Unitario directo (sin Spring). Cubre 8 handlers + errores múltiples |
| `JwtTokenProviderTest` | 12 | Unitario directo. Generate, extract, validate, edge cases |
| `DataInitializerTest` | 7 | `@ExtendWith(MockitoExtension.class)`. Primera ejecución, idempotencia |
| `SysMaderaApplicationTests` | 1 | `@SpringBootTest`. Context loads |

### Test Utility

`TestDataFactory.java` — Clase utilitaria para crear datos de prueba.

---

## 3. MAPA COMPLETO: Endpoint ↔ Test Coverage

| Endpoint | Controller Test | Integration Test |
|---|---|---|
| `POST /auth/login` | ✅ 3 tests | ❌ |
| `POST /auth/register` | ✅ 3 tests | ❌ |
| `POST /clientes` | ✅ 3 tests | ✅ 3 |
| `GET /clientes/{id}` | ✅ 2 tests | ✅ 2 |
| `GET /clientes` | ✅ 2 tests | ✅ 1 |
| `PUT /clientes/{id}` | ✅ 1 test | ✅ 3 |
| `DELETE /clientes/{id}` | ✅ 3 tests | ✅ 2 |
| `POST /muebles` | ✅ 3 tests | ✅ 3 |
| `GET /muebles/{id}` | ✅ 3 tests | ✅ 2 |
| `GET /muebles` | ✅ 3 tests | ✅ 2 |
| `PUT /muebles/{id}` | ✅ 3 tests | ✅ 2 |
| `DELETE /muebles/{id}` | ✅ 2 tests | ✅ 4 |
| `POST /pedidos` | ✅ 2 tests | ✅ 5 |
| `GET /pedidos/{id}` | ✅ 2 tests | ✅ 2 |
| `GET /pedidos` | ✅ 2 tests | ✅ 1 |
| `GET /pedidos/cliente/{clientId}` | ✅ 1 test | ❌ |
| `PATCH /pedidos/{id}/estado` | ✅ 2 tests | ✅ 2 |
| `POST /facturas` | ✅ 3 tests | ✅ 3 |
| `GET /facturas/{id}` | ✅ 2 tests | ✅ 2 |
| `GET /facturas/orden/{orderId}` | ✅ 2 tests | ✅ 2 |
| `GET /facturas` | ✅ 2 tests | ✅ 1 |
| `POST /facturas/{id}/pago` | ✅ 4 tests | ✅ 5 |
| `POST /inventario-madera` | ✅ 3 tests | ✅ 3 |
| `GET /inventario-madera/{id}` | ✅ 2 tests | ✅ 2 |
| `GET /inventario-madera` | ✅ 2 tests | ✅ 1 |
| `PUT /inventario-madera/{id}` | ✅ 2 tests | ✅ 3 |
| `DELETE /inventario-madera/{id}` | ✅ 2 tests | ✅ 2 |
| `GET /inventario-madera/bajo-stock` | ✅ 1 test | ✅ 2 |
| **`GET /dashboard/admin`** | ❌ **0 tests** | ❌ **0 tests** |
| **`GET /dashboard/empleado`** | ❌ **0 tests** | ❌ **0 tests** |
| **`GET /reportes/ventas`** | ❌ **0 tests** | ❌ **0 tests** |
| **`GET /reportes/inventario`** | ❌ **0 tests** | ❌ **0 tests** |
| **`GET /reportes/mas-vendidos`** | ❌ **0 tests** | ❌ **0 tests** |

**Total endpoints core: 33 | Cubiertos: 28 (85%) | Sin test: 5 (Dashboard 2 + Report 3)**

---

## 4. BRECHA COMPLETA: Lo que falta exactamente

### 🔴 PRIORIDAD ALTA — Componentes sin test

| Clase | Métodos | Impacto |
|---|---|---|
| **`JwtAuthenticationFilter`** | `doFilterInternal`, `extractToken` | Filtro central de auth — sin test, la autenticación completa no está cubierta |
| **`CustomUserDetailsService`** | `loadUserByUsername` | Carga usuarios para Spring Security — 0 tests |
| **`JwtEntryPoint`** | `commence` | Manejador 401 — 0 tests |
| **`SecurityConfig`** | `securityFilterChain`, `corsConfigurationSource`, `passwordEncoder`, `authenticationManager` | Endpoints públicos vs protegidos, CORS, CSRF — no verificado |
| **`DashboardController`** | `getAdminDashboard`, `getEmployeeDashboard` | 2 endpoints sin cobertura |
| **`DashboardServiceImpl`** | `getAdminDashboard`, `getEmployeeDashboard` | Servicio completo sin test |
| **`ReportController`** | `getSalesReport`, `getInventoryReport`, `getTopSellingFurniture` | 3 endpoints sin cobertura |
| **`ReportServiceImpl`** | `getSalesReport`, `getInventoryReport`, `getTopSellingFurniture` | Servicio completo sin test |
| **`OrderDetailRepository`** | `findByOrderId`, `findByFurnitureId`, `findTopSellingFurniture` | Único repository sin test |

### 🔴 PRIORIDAD ALTA — Bugs conocidos

| Ítem | Archivo | Detalle |
|---|---|---|
| **Test vacío** | `InvoiceControllerTest.java` | `shouldReturn400_whenNegativeAmount()` — cuerpo vacío, pasa sin validar nada |
| **ClassCastException potencial** | `GlobalExceptionHandler.java` | `handleValidationErrors` castea a `FieldError` directamente. Si `MethodArgumentNotValidException` contiene un `ObjectError` genérico (no `FieldError`), lanza `ClassCastException` |

### 🟡 PRIORIDAD MEDIA — Métodos service sin probar

| Clase | Método |
|---|---|
| `ClientServiceImpl` | `findAll(int page, int size, String sort, String direction, String search)` |

### 🟡 PRIORIDAD MEDIA — Escenarios negativos faltantes en controllers

| Controller | Escenarios faltantes |
|---|---|
| `AuthController` | 401 (bad credentials), 409 (duplicate user) |
| `ClientController` | PUT 404 (cliente no existe en update), PUT 409 (email/rfc conflict en update) |
| `FurnitureController` | PUT validation (enviar campos inválidos en update) |
| `OrderController` | PATCH 404 (orden no existe en updateStatus), POST 500 |
| `InvoiceController` | (el test vacío de pago negativo) |
| `WoodInventoryController` | PUT validation, GET / acceso control (bajo-stock con EMPLEADO/CLIENTE) |

### 🟡 PRIORIDAD MEDIA — Mejoras transversales

| Ítem | Descripción |
|---|---|
| **401 Unauthorized en controllers** | Todos usan `addFilters = false` en `@AutoConfigureMockMvc`. Nadie prueba qué pasa cuando se envía request sin token JWT |
| **Estructura de error @Valid** | Todos los tests de validación solo verifican status code 400. Nunca se verifica que el JSON de error contenga los field names y mensajes correctos |
| **DELETE con FK** | No hay tests que prueben eliminar un cliente que tiene pedidos, o un mueble que está en orderDetails |
| **Ordenamiento en paginación** | Los tests de GET pasan sort/direction pero nunca verifican que los resultados vengan en el orden esperado |

### 🟢 PRIORIDAD BAJA

| Ítem | Descripción |
|---|---|
| `ClientRepository.findByUserId` | Único método custom de repositorio sin test |
| `Thread.sleep(10)` en `AuditIntegrationTest` | Frágil y lento. Mejor usar `Clock` fijo |
| Audit para `Role` y `OrderDetail` | No tienen `@EntityListeners(AuditingEntityListener.class)`, pero si se agregan después, no hay test |
| `WoodSurplusServiceImpl` sin excepciones | El service no valida nada, pero no hay test para null pointer |
| Ataque JWT algorithm `none` | `JwtTokenProvider.validateToken` no prueba este vector |
| CORS headers | `Access-Control-Allow-Origin` no verificado en ningún test |
| Tests concurrentes | Stock reduction race conditions no probadas |

---

## 5. RESUMEN DE COBERTURA

| Capa | Archivos con test / total | Tests aprox. | Cobertura estimada |
|---|---|---|---|
| **Repository** | 8/9 | 139 | ~90% |
| **Service** | 7/9 | 108 | ~85% |
| **Controller** | 6/8 | 96 | ~85% endpoints |
| **Integration** | 6 | 98 | ~60% flujos |
| **Security** | 1/4 (solo JwtTokenProvider) | 12 | ~30% |
| **Exception** | 1/1 | 9 | ~100% |
| **Config** | 1/4 (solo DataInitializer) | 7 | ~25% |
| **Total** | **32 archivos** | **~459 tests** | |

---

## 6. ORDEN SUGERIDO PARA COMPLETAR TESTS

| # | Prioridad | Qué hacer |
|---|---|---|
| 1 | 🔴 | `CustomUserDetailsServiceTest` |
| 2 | 🔴 | `JwtAuthenticationFilterTest` |
| 3 | 🔴 | `JwtEntryPointTest` |
| 4 | 🔴 | `SecurityConfigIntegrationTest` (o test directo) |
| 5 | 🔴 | `DashboardServiceImplTest` + `DashboardControllerTest` |
| 6 | 🔴 | `ReportServiceImplTest` + `ReportControllerTest` |
| 7 | 🔴 | `OrderDetailRepositoryTest` |
| 8 | 🔴 | Fix `shouldReturn400_whenNegativeAmount` vacío |
| 9 | 🔴 | Fix `ClassCastException` en `handleValidationErrors` |
| 10 | 🟡 | `ClientServiceImpl.findAll` test |
| 11 | 🟡 | Escenarios 401 en controllers (quitar `addFilters=false`) |
| 12 | 🟡 | Escenarios negativos: PUT 404/409, PATCH 404, etc. |
| 13 | 🟡 | Validar estructura de errores @Valid (field names + messages) |
| 14 | 🟡 | DELETE con FK constraints |
| 15 | 🟡 | Verificar ordenamiento en paginación |
| 16 | 🟢 | `ClientRepository.findByUserId` test |
| 17 | 🟢 | `Thread.sleep` → Clock fijo |
| 18 | 🟢 | JWT algorithm `none` attack test |
| 19 | 🟢 | CORS headers test |
