# Auditoría de Calidad de Testing — sys_madera

## Resumen

| Capa | Archivos Test | Tests | Cobertura aprox. |
|---|---|---|---|
| Repository | 8/9 | 139 | ~90% |
| Service | 7/9 servicios | 108 | ~85% |
| Controller | 6/8 controladores | 96 | ~85% endpoints |
| Integration | 6 archivos | 98 | ~60% flujos |
| Security/Exception | 3 archivos | 18 | ~40% |

**Total tests: ~459**

---

## 1. Qué está bien

- **Patrón consistente**: Todos los tests usan `@Nested`, `@DisplayName`, `BDDMockito.given()`, `ArgumentCaptor`, `@DataJpaTest`/`@WebMvcTest`. Código legible y mantenible.
- **Repository**: 8/9 repos probados. Métodos custom con escenarios: existente, no encontrado, empty, case-insensitive, paginación. Tests de constraints unique en Role, User, Client, Order.
- **Service**: Uso extensivo de Mockito + `ArgumentCaptor`. Escenarios de excepción cubiertos en todos los servicios principales. `@ParameterizedTest` en 3 archivos (Auth, Client, Invoice, WoodInventory). InvoiceServiceImplTest es el más completo (21 tests).
- **Controller**: 28/33 endpoints cubiertos (85%). Tests de acceso por rol (ADMIN/EMPLEADO/CLIENTE). Validación de estructura JSON con `jsonPath`. Paginación probada en todos los GET paginados.
- **GlobalExceptionHandler**: 9 tests cubriendo los 8 handlers. Status code, mensaje, timestamp, errors map verificados.
- **GlobalExceptionHandler**: 9 tests directos cubriendo los 8 handlers con status code, mensaje, timestamp, errors map y `null` para no-validación.
- **JwtTokenProvider**: 7 edge cases (expired, malformed, wrong key, null, empty, dots, special chars).
- **DataInitializer**: Idempotencia probada (3 tests).
- **AuditIntegrationTest**: 28 tests, 7 entidades, verifica createdAt/updatedAt.
- **Soft delete (Furniture)**: 13 tests entre repository, service, controller e integration.
- **Invoice payment lifecycle**: PENDIENTE → PAGADA_PARCIAL → PAGADA, overpayment, duplicate payment.

---

## 2. Qué falta (por prioridad)

### 🔴 Prioridad Alta

| Ítem | Detalle |
|---|---|
| **`JwtAuthenticationFilter` sin test** | Filtro central de autenticación → 0 tests |
| **`CustomUserDetailsService` sin test** | Carga usuarios para Spring Security → 0 tests |
| **`SecurityConfig` sin test directo** | Endpoints públicos vs protegidos, CORS, CSRF, stateless → no verificado |
| **`JwtEntryPoint` sin test** | Manejador 401 → 0 tests |
| **`DashboardController` + `ReportController` sin tests** | 5 endpoints sin cobertura |
| **`ReportServiceImpl` + `DashboardServiceImpl` sin tests** | 2 servicios completos sin cobertura (reportes, dashboard) |
| **`OrderDetailRepository` sin test** | Único repository sin test (3 métodos custom: `findByOrderId`, `findByFurnitureId`, `findTopSellingFurniture`) |
| **`ClientRepository.findByUserId()` sin test** | Único método custom sin probar entre los repos probados |
| **`InvoiceControllerTest.shouldReturn400_whenNegativeAmount()` vacío** | Test sin cuerpo, pasa por inercia |
| **`MethodArgumentNotValidException` con `ObjectError` no-`FieldError`** | `handleValidationErrors` castea a `FieldError` sin verificar tipo → `ClassCastException` |

### 🟡 Prioridad Media

| Ítem | Detalle |
|---|---|
| **401 Unauthorized no probado en ningún controller** | `addFilters = false` en todos. Nadie prueba qué pasa sin token |
| **AuthController: bad credentials (401), duplicate user (409)** | Solo prueba 200/400/500 |
| **ClientController: PUT 404, 409 Conflict** | Faltan escenarios negativos |
| **FurnitureController: PUT validation** | No inválido en update |
| **OrderController: PATCH 404, POST 500** | Faltan escenarios |
| **Invoice: test de pago negativo vacío** | Fix urgente |
| **WoodInventory: PUT validation, GET / y /bajo-stock access control** | No probados |
| **`ClientServiceImpl.findAll` sin test** | Único método service sin probar entre los testeados |
| **Tests de `@Valid` violando campos** | Solo status code 400, nunca se verifica estructura del error (field names, messages) |
| **DELETE en entidades con relaciones (FK)** | Cliente con pedidos, mueble con orderDetails, etc. |
| **Paginación: ordenamiento ascendente/descendente no verificado** | Solo se pasan params, no se verifica el orden real |

### 🟢 Prioridad Baja

| Ítem | Detalle |
|---|---|
| **`Thread.sleep(10)` en AuditIntegrationTest** | Frágil, mejor usar `Clock` fijo |
| **`WoodSurplusServiceImpl` sin test de excepciones** | Service no tiene validaciones, pero podría tener null/empty |
| **`OrderDetail` y `Role` sin audit tests** | Si tienen `@EntityListeners`, no se prueban |
| **Tests concurrentes** | Stock reduction race conditions no probadas |
| **Bulk operations** | No existen endpoints batch |
| **File upload/download** | No existen endpoints |
| **Token con algoritmo `none` (JWT)** | Ataque de confusión de algoritmo no probado |
| **CORS: `Access-Control-Allow-Origin: *`** | No verificado |

---

## 3. Cobertura por módulo (estimada)

| Módulo | Cobertura |
|---|---|
| **Repository** | ~90% — 8/9 archivos, todos métodos custom probados excepto `ClientRepository.findByUserId` |
| **Service** | ~85% — 7/9 servicios. `ReportService` y `DashboardService` en 0%. `ClientServiceImpl.findAll` sin test |
| **Controller** | ~85% endpoints (28/33). Dashboard y Report en 0%. Faltan escenarios negativos en controllers existentes |
| **Integration** | ~60% — CRUD básico cubierto en 5 entidades. Sin tests de auth, validación, ni constraints referenciales |
| **Security (JWT)** | ~30% — `JwtTokenProvider` bien cubierto. Filtro, UserDetailsService, EntryPoint, SecurityConfig en 0% |
| **Exception Handling** | ~100% — Todos los handlers probados con status, mensaje, estructura |
| **Auditoría** | ~90% — 7/9 entidades audit probadas (falta Role, OrderDetail) |
| **Soft Delete** | ~100% para Furniture (única entidad con soft delete) |
| **Paginación** | ~80% — Probada en controllers pero sin verificar orden real |
| **Validaciones (@Valid)** | ~30% — Solo status code, nunca estructura detallada del error |

---

## 4. Tests adicionales recomendados

### Orden sugerido de implementación

1. **`CustomUserDetailsServiceTest`** — user found, not found, multiple roles, disabled user
2. **`JwtAuthenticationFilterTest`** — valid token, invalid, expired, missing header, user not found
3. **`JwtEntryPointTest`** — 401 status, content-type JSON, body structure
4. **`SecurityConfigIntegrationTest`** — `@SpringBootTest(RANDOM_PORT)` + `TestRestTemplate`: públicos 200 sin auth, protegidos 401 sin auth, CORS headers
5. **`DashboardControllerTest` + `DashboardServiceImplTest`**
6. **`ReportControllerTest` + `ReportServiceImplTest`**
7. **`OrderDetailRepositoryTest`** — `findByOrderId`, `findByFurnitureId`, `findTopSellingFurniture`
8. **Fix `InvoiceControllerTest.shouldReturn400_whenNegativeAmount`** — implementar el test vacío
9. **`ClientRepository.findByUserId` test** — agregar al `ClientRepositoryTest` existente
10. **`MethodArgumentNotValidException` con `ObjectError` no-`FieldError`** — test que atrape el `ClassCastException`
11. **Controller: escenarios 401 sin autenticación** — quitar `addFilters = false` en al menos 1 test por controller o agregar security integration test
12. **Controller: `@ParameterizedTest` para validación de campos** — probar múltiples combinaciones inválidas y verificar estructura del error

---

## 5. Prioridad resumida

| Prioridad | Acciones |
|---|---|
| **🔴 Alta** | Filtrar JWT + UserDetailsService + EntryPoint + SecurityConfig (4 componentes críticos sin test). Dashboard/Report controllers + services. OrderDetailRepository. Fix test vacío. Bug ClassCastException en validación. |
| **🟡 Media** | 401 en controllers. Escenarios negativos faltantes (404/409/400). ClientServiceImpl.findAll. Validar estructura de errores 400. DELETE con FK. Ordenamiento en paginación. |
| **🟢 Baja** | Thread.sleep → Clock fijo. WoodSurplus sin excepciones. Audit para Role/OrderDetail. Concurrentes. CORS. JWT algorithm none. |
