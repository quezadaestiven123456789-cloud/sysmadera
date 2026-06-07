# Mejoras e Implementaciones Pendientes

> Proyecto: WoodManager — sys_madera  
> Tests actuales: **238, 0 fallos**  
> Fecha: 2026-06-06

---

## 1. Código Faltante en `src/main/` (Producción)

### 1.1 Stock de `WoodInventory` nunca se descuenta al crear orden

**Archivo:** `src/main/java/com/madera/sys_madera/service/impl/OrderServiceImpl.java`

**Problema:** Al crear una orden se descuenta `Furniture.stockQuantity` (se añadió en el fix reciente), pero `WoodInventory.quantity` (la madera cruda) jamás se toca. La materia prima nunca se reduce.

**Implementación necesaria:** En el `map()` de `OrderServiceImpl.create()`, tras validar stock y deducir de `Furniture`, buscar `WoodInventory` por `furniture.getWoodType()` y deducir la cantidad proporcional. Requiere inyectar `WoodInventoryRepository` en `OrderServiceImpl`.

```java
// Dentro del stream.map(), después de furniture.setStockQuantity(...)
if (furniture.getWoodType() != null) {
    woodInventoryRepository.findByWoodTypeIgnoreCase(furniture.getWoodType())
            .ifPresent(wood -> {
                BigDecimal woodDeduction = BigDecimal.valueOf(detailRequest.quantity());
                wood.setQuantity(wood.getQuantity().subtract(woodDeduction));
                woodInventoryRepository.save(wood);
            });
}
```

**Prioridad:** Alta — funcionalidad crítica faltante.

---

### 1.2 `FurnitureServiceImpl.findAll` filtra `active=true` inconsistentemente

**Archivo:** `src/main/java/com/madera/sys_madera/service/impl/FurnitureServiceImpl.java`

**Problema:** Cuando no hay search ni category, usa `furnitureRepository.findByActiveTrue(pageable)`. Pero cuando hay search usa `findByNameContainingIgnoreCase(name, pageable)` y cuando hay category usa `findByCategoryIgnoreCase(category, pageable)` — ninguno filtra por `active=true`. Devuelve muebles inactivos en esos casos.

**Implementación necesaria:**
- Crear métodos en `FurnitureRepository`: `findByNameContainingIgnoreCaseAndActiveTrue`, `findByCategoryIgnoreCaseAndActiveTrue`
- O bien unificar con `@Query("WHERE (active = true) AND (:name IS NULL OR name LIKE %:name%) AND (:category IS NULL OR category = :category)")`

**Prioridad:** Media — bug funcional en la lógica de listado.

---

### 1.3 Seeders para desarrollo

**Archivo:** `src/main/java/com/madera/sys_madera/config/DataInitializer.java`

**Problema:** `DataInitializer` solo crea 3 roles (`ROLE_ADMIN`, `ROLE_EMPLEADO`, `ROLE_CLIENTE`). No hay datos demo: clientes, muebles, inventario, usuarios. Al clonar el repo y ejecutar con perfil `dev`, la BD arranca vacía.

**Implementación necesaria:** Un segundo `CommandLineRunner` con `@Profile("dev")` que cree:
- 1 usuario admin (admin/admin123)
- 1 usuario empleado
- 1 usuario cliente
- 3-4 clientes
- 5-6 muebles con stock variado
- 3-4 tipos de madera en inventario
- 1-2 órdenes de ejemplo

**Prioridad:** Media — impacto directo en onboarding y desarrollo diario.

---

### 1.4 Secretos en texto plano

**Archivo:** `src/main/resources/application-dev.properties`

**Problema:** `spring.datasource.password=lokuw` y `app.jwt.secret=...` están en texto plano en el repositorio. Si el repo se vuelve público o alguien tiene acceso, puede firmar JWT contra producción.

**Implementación necesaria:**
```properties
spring.datasource.password=${DB_PASSWORD}
app.jwt.secret=${JWT_SECRET}
```
Documentar las variables de entorno requeridas en un `.env.example` o en `README.md`.

Además, el JWT secret de test no debería ser el mismo que el de desarrollo.

**Prioridad:** Media-Alta — seguridad.

---

## 2. Tests Faltantes en `src/test/`

### 2.1 Repository Tests

Actualmente solo existen `ClientRepositoryTest` (18) y `OrderRepositoryTest` (18). Faltan:

| Archivo | Métodos a testear | Tests estimados |
|---------|------------------|-----------------|
| `FurnitureRepositoryTest` | `findByActiveTrue`, `findByNameContainingIgnoreCase`, `findByCategoryIgnoreCase`, `findByStockQuantityLessThan`, `existsByName`, `save`, CRUD, unique constraints | ~18 |
| `WoodInventoryRepositoryTest` | `findByWoodTypeIgnoreCase`, CRUD | ~8 |
| `InvoiceRepositoryTest` | `findByOrderId`, `findByStatus`, `countByStatus`, CRUD | ~14 |
| `UserRepositoryTest` | `findByUsername`, `existsByUsername`, CRUD | ~10 |
| `RoleRepositoryTest` | `findByName`, CRUD | ~6 |

**Total estimado:** ~56 tests nuevos.

**Prioridad:** Alta — cobertura mínima en capa de datos.

---

### 2.2 Service Tests

**OrderServiceTest** — Verificar stock deduction (urge tras el fix):
- Capturar `Furniture` y verificar `setStockQuantity(7)` cuando stock inicial es 10 y se ordenan 3
- Verificar que se persiste el cambio (`verify(furnitureRepository).save(furniture)` con stock reducido)

**WoodInventoryServiceImpl.update** — Edge cases faltantes (4-5 tests):
- `shouldUpdate_whenNegativeQuantity`
- `shouldUpdate_whenZeroQuantity`
- `shouldUpdate_whenNullSupplierAndDescription`
- `shouldUpdate_whenNegativeUnitPrice`
- `shouldUpdate_whenWoodTypeChangedAndNoConflict`

**WoodSurplus** — Modelo existe pero **0 tests** en ninguna capa:
- `WoodSurplusService` (si existe) o el CRUD básico
- `WoodSurplusRepositoryTest`
- Crear, consultar, eliminar, marcar no disponible

**Total estimado:** ~15 tests nuevos.

**Prioridad:** Alta.

---

### 2.3 Integration Tests

Solo existe `OrderIntegrationTest` (11). Faltan:

| Test | Flujos críticos | Tests estimados |
|------|----------------|-----------------|
| `ClientIntegrationTest` | CRUD completo, email/rfc duplicado, búsqueda | ~8 |
| `FurnitureIntegrationTest` | CRUD, stock deduction desde orden, soft-delete, categorías | ~10 |
| `InvoiceIntegrationTest` | CRUD, registerPayment, estado cambia, validaciones | ~10 |
| `WoodInventoryIntegrationTest` | CRUD, lowStock, woodType duplicado | ~8 |

**Total estimado:** ~36 tests nuevos.

**Prioridad:** Alta.

---

### 2.4 Controller Tests — `PagedResponse<T>` Serialization

**Problema:** `record PagedResponse<T>` no serializa correctamente en `@WebMvcTest` porque Jackson no resuelve el tipo genérico. Los tests de endpoints paginados solo verifican `status 200`, nunca el JSON de respuesta.

**Posibles soluciones (en test):**
1. Agregar Jackson `@JsonTypeInfo` al `PagedResponse` (toca production code)
2. Usar `@JsonTest` con configuración específica para testear serialización
3. Crear un `ObjectMapper` bean de test con `Jackson2ObjectMapperBuilder` que maneje genéricos
4. Usar `TypeFactory` de Jackson para construir `JavaType` con parámetros de tipo

**Prioridad:** Media — tests de paginación parcialmente ciegos.

---

### 2.5 `GlobalExceptionHandler` — Sin test directo

**Problema:** Cada `@ExceptionHandler` (ResourceNotFound, BadRequest, Duplicate, AccessDenied, MethodArgumentNotValid, genérico) no tiene test unitario.

**Implementación:** Un `@WebMvcTest` con un controller dummy que lance cada excepción, o tests de controller existentes que cubran los paths de error. Ya hay cobertura parcial en los controller tests.

**Prioridad:** Baja — ya cubierto indirectamente por controller tests.

---

## 3. Mejoras de Calidad

### 3.1 `TestDataFactory` Compartida

**Problema:** Cada test declara `private Client buildClient()` / `private ClientRequest buildRequest()` duplicados. 14 test classes → al menos 14 métodos `buildXxx` casi idénticos.

**Implementación:**
```java
// src/test/java/com/madera/sys_madera/util/TestDataFactory.java
public class TestDataFactory {
    public static Client createClient() { ... }
    public static ClientRequest createClientRequest() { ... }
    public static Furniture createFurniture() { ... }
    public static FurnitureRequest createFurnitureRequest() { ... }
    // ...
}
```

**Prioridad:** Media — calidad de código.

---

### 3.2 `@ParameterizedTest` para edge cases repetitivos

**Problema:** `shouldCreateWithNegativeQuantity`, `shouldCreateWithZeroQuantity`, `shouldCreateWithNegativeUnitPrice` son 3 métodos casi idénticos en `WoodInventoryServiceImplTest`.

**Implementación:**
```java
@ParameterizedTest
@CsvSource({
    "Pino, -50.00, 10.00, quantity no debe ser negativo",
    "Pino, 0.00, 10.00, quantity no debe ser cero",
    "Pino, 50.00, -10.00, unitPrice no debe ser negativo"
})
void shouldFail_whenInvalidInput(String woodType, String qty, String price, String msg) { ... }
```

Requiere que el service lance excepciones para esos casos (actualmente no lo hace — acepta negativos). Alternativa: documentar que el service no valida porque confía en `@Valid` del controller.

**Prioridad:** Baja.

---

### 3.3 JaCoCo Coverage Gate

**Implementación en `pom.xml`:**
```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.12</version>
    <executions>
        <execution><goals><goal>prepare-agent</goal></goals></execution>
        <execution>
            <id>report</id>
            <phase>verify</phase>
            <goals><goal>report</goal></goals>
        </execution>
        <execution>
            <id>check</id>
            <goals><goal>check</goal></goals>
            <configuration>
                <rules>
                    <rule><element>BUNDLE</element>
                        <limits><limit><counter>LINE</counter><value>COVERED_RATIO</value><minimum>0.70</minimum></limit></limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

**Prioridad:** Media — visibilidad de cobertura.

---

## 4. Resumen de Esfuerzo

| Categoría | Ítems | Tests nuevos | Código nuevo (est.) |
|-----------|-------|-------------|---------------------|
| Bug fixes (main) | 2 | — | ~30 líneas |
| Seeders dev | 1 | — | ~100 líneas |
| Secretos | 1 | — | ~5 líneas |
| Repository tests | 5 clases | ~56 | ~800 líneas |
| Service tests | 3 faltantes | ~15 | ~300 líneas |
| Integration tests | 4 clases | ~36 | ~600 líneas |
| PagedResponse fix | 1 | — | ~20 líneas |
| TestDataFactory | 1 | — | ~150 líneas |
| JaCoCo | 1 | — | ~30 líneas pom.xml |
| **Total** | **~19** | **~107** | **~2000 líneas** |

> **Leyenda:**  
> 🟢 Alta prioridad — bugs funcionales o cobertura ausente  
> 🟡 Media prioridad — calidad, mantenibilidad, seguridad  
> 🔵 Baja prioridad — nice to have
