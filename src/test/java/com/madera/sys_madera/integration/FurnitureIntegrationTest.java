package com.madera.sys_madera.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.madera.sys_madera.model.Client;
import com.madera.sys_madera.model.Furniture;
import com.madera.sys_madera.repository.ClientRepository;
import com.madera.sys_madera.repository.FurnitureRepository;
import com.madera.sys_madera.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(roles = "ADMIN")
@DisplayName("Furniture Integration (full stack)")
class FurnitureIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FurnitureRepository furnitureRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        furnitureRepository.deleteAll();
        clientRepository.deleteAll();
    }

    @Nested
    @DisplayName("POST /api/v1/muebles")
    class Create {

        @Test
        @DisplayName("should create furniture and persist in database")
        void shouldCreateFurniture() throws Exception {
            var body = """
                    {
                        "name": "Mesa de Roble",
                        "description": "Mesa de roble macizo",
                        "price": 1500.00,
                        "woodType": "Roble",
                        "category": "Mesas",
                        "stockQuantity": 10
                    }
                    """;

            mockMvc.perform(post("/api/v1/muebles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("Mesa de Roble"))
                    .andExpect(jsonPath("$.price").value(1500.00))
                    .andExpect(jsonPath("$.stockQuantity").value(10))
                    .andExpect(jsonPath("$.active").value(true))
                    .andExpect(jsonPath("$.id").isNumber());

            assertThat(furnitureRepository.findByNameContainingIgnoreCase("Mesa", null)
                    .getContent()).isNotEmpty();
        }

        @Test
        @DisplayName("should throw 409 when name already exists")
        void shouldThrow409_whenDuplicateName() throws Exception {
            furnitureRepository.save(Furniture.builder()
                    .name("Mesa de Roble").description("Original")
                    .price(new BigDecimal("1000.00")).stockQuantity(5).active(true)
                    .build());

            var body = """
                    {
                        "name": "Mesa de Roble",
                        "price": 1500.00,
                        "stockQuantity": 10
                    }
                    """;

            mockMvc.perform(post("/api/v1/muebles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value(
                            org.hamcrest.Matchers.containsString("Mesa de Roble")));
        }

        @Test
        @DisplayName("should default stock to 0 when not provided")
        void shouldDefaultStockToZero() throws Exception {
            var body = """
                    {
                        "name": "Silla Simple",
                        "price": 500.00
                    }
                    """;

            mockMvc.perform(post("/api/v1/muebles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.stockQuantity").value(0));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/muebles/{id}")
    class FindById {

        @Test
        @DisplayName("should return furniture when exists")
        void shouldReturnFurniture_whenExists() throws Exception {
            var saved = furnitureRepository.save(Furniture.builder()
                    .name("Cómoda").description("Cómoda de pino")
                    .price(new BigDecimal("3000.00")).stockQuantity(5)
                    .category("Dormitorio").active(true)
                    .build());

            mockMvc.perform(get("/api/v1/muebles/{id}", saved.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Cómoda"))
                    .andExpect(jsonPath("$.category").value("Dormitorio"));
        }

        @Test
        @DisplayName("should throw 404 when not found")
        void shouldThrow404_whenNotFound() throws Exception {
            mockMvc.perform(get("/api/v1/muebles/{id}", 999L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(
                            org.hamcrest.Matchers.containsString("Mueble")));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/muebles")
    class FindAll {

        @Test
        @DisplayName("should return paginated furniture")
        void shouldReturnPaginatedFurniture() throws Exception {
            furnitureRepository.save(Furniture.builder()
                    .name("Mesa").price(new BigDecimal("1000.00")).stockQuantity(5).active(true).build());
            furnitureRepository.save(Furniture.builder()
                    .name("Silla").price(new BigDecimal("500.00")).stockQuantity(10).active(true).build());

            mockMvc.perform(get("/api/v1/muebles")
                            .param("page", "0").param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.totalElements").value(2));
        }

        @Test
        @DisplayName("should filter by search term")
        void shouldFilterBySearch() throws Exception {
            furnitureRepository.save(Furniture.builder()
                    .name("Mesa de Roble").price(new BigDecimal("1500.00")).stockQuantity(5).active(true).build());
            furnitureRepository.save(Furniture.builder()
                    .name("Silla de Roble").price(new BigDecimal("500.00")).stockQuantity(10).active(true).build());

            mockMvc.perform(get("/api/v1/muebles")
                            .param("search", "Mesa"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.content[0].name").value("Mesa de Roble"));
        }

        @Test
        @DisplayName("should filter by category")
        void shouldFilterByCategory() throws Exception {
            furnitureRepository.save(Furniture.builder()
                    .name("Mesa").price(new BigDecimal("1500.00")).stockQuantity(5)
                    .category("Mesas").active(true).build());
            furnitureRepository.save(Furniture.builder()
                    .name("Silla").price(new BigDecimal("500.00")).stockQuantity(10)
                    .category("Sillas").active(true).build());
            furnitureRepository.save(Furniture.builder()
                    .name("Cama").price(new BigDecimal("4000.00")).stockQuantity(3)
                    .category("Dormitorio").active(true).build());

            mockMvc.perform(get("/api/v1/muebles")
                            .param("category", "Sillas"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.content[0].name").value("Silla"));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/muebles/{id}")
    class Update {

        @Test
        @DisplayName("should update furniture and persist changes")
        void shouldUpdateFurniture() throws Exception {
            var saved = furnitureRepository.save(Furniture.builder()
                    .name("Mesa Original").description("Original")
                    .price(new BigDecimal("1000.00")).stockQuantity(5).active(true).build());

            var body = """
                    {
                        "name": "Mesa Actualizada",
                        "description": "Actualizada",
                        "price": 2000.00,
                        "woodType": "Caoba",
                        "category": "Mesas Premium",
                        "stockQuantity": 8
                    }
                    """;

            mockMvc.perform(put("/api/v1/muebles/{id}", saved.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Mesa Actualizada"))
                    .andExpect(jsonPath("$.price").value(2000.00))
                    .andExpect(jsonPath("$.stockQuantity").value(8));

            var refreshed = furnitureRepository.findById(saved.getId()).orElseThrow();
            assertThat(refreshed.getName()).isEqualTo("Mesa Actualizada");
            assertThat(refreshed.getPrice()).isEqualByComparingTo(new BigDecimal("2000.00"));
        }

        @Test
        @DisplayName("should throw 404 when updating non-existent furniture")
        void shouldThrow404_whenNotFound() throws Exception {
            var body = """
                    {"name": "Nadie", "price": 100.00}
                    """;

            mockMvc.perform(put("/api/v1/muebles/{id}", 999L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(
                            org.hamcrest.Matchers.containsString("Mueble")));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/muebles/{id}")
    class Delete {

        @Test
        @DisplayName("should soft-delete furniture (set active=false)")
        void shouldSoftDeleteFurniture() throws Exception {
            var saved = furnitureRepository.save(Furniture.builder()
                    .name("Eliminar").description("Será desactivado")
                    .price(new BigDecimal("1000.00")).stockQuantity(5).active(true).build());

            mockMvc.perform(delete("/api/v1/muebles/{id}", saved.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Mueble desactivado exitosamente"));

            var refreshed = furnitureRepository.findById(saved.getId()).orElseThrow();
            assertThat(refreshed.getActive()).isFalse();
        }

        @Test
        @DisplayName("should throw 404 when deleting non-existent furniture")
        void shouldThrow404_whenNotFound() throws Exception {
            mockMvc.perform(delete("/api/v1/muebles/{id}", 999L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(
                            org.hamcrest.Matchers.containsString("Mueble")));
        }
    }

    @Nested
    @DisplayName("Soft Delete")
    class SoftDelete {

        @Test
        @DisplayName("should keep record in database after soft delete")
        void shouldKeepRecordInDatabase() throws Exception {
            var saved = furnitureRepository.save(Furniture.builder()
                    .name("ParaBorrar").price(new BigDecimal("500.00"))
                    .stockQuantity(5).active(true).build());
            long countBefore = furnitureRepository.count();

            mockMvc.perform(delete("/api/v1/muebles/{id}", saved.getId()))
                    .andExpect(status().isOk());

            assertThat(furnitureRepository.count()).isEqualTo(countBefore);
        }

        @Test
        @DisplayName("findById should return soft-deleted furniture")
        void findByIdShouldReturnSoftDeleted() throws Exception {
            var saved = furnitureRepository.save(Furniture.builder()
                    .name("Inactivo").price(new BigDecimal("500.00"))
                    .stockQuantity(5).active(false).build());

            mockMvc.perform(get("/api/v1/muebles/{id}", saved.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.active").value(false));
        }

        @Test
        @DisplayName("findAll should include soft-deleted furniture")
        void findAllShouldIncludeInactive() throws Exception {
            furnitureRepository.save(Furniture.builder()
                    .name("Activo").price(new BigDecimal("100.00"))
                    .stockQuantity(5).active(true).build());
            furnitureRepository.save(Furniture.builder()
                    .name("Inactivo").price(new BigDecimal("200.00"))
                    .stockQuantity(3).active(false).build());

            mockMvc.perform(get("/api/v1/muebles")
                            .param("page", "0").param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.totalElements").value(2));
        }

        @Test
        @DisplayName("search should include inactive records")
        void searchShouldIncludeInactive() throws Exception {
            furnitureRepository.save(Furniture.builder()
                    .name("Mesa Roble").price(new BigDecimal("1000.00"))
                    .stockQuantity(5).active(true).build());
            furnitureRepository.save(Furniture.builder()
                    .name("Mesa Inactiva").price(new BigDecimal("500.00"))
                    .stockQuantity(3).active(false).build());

            mockMvc.perform(get("/api/v1/muebles")
                            .param("search", "Mesa"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.totalElements").value(2));
        }

        @Test
        @DisplayName("category filter should include inactive records")
        void categoryFilterShouldIncludeInactive() throws Exception {
            furnitureRepository.save(Furniture.builder()
                    .name("Mesa Activa").price(new BigDecimal("1000.00"))
                    .stockQuantity(5).category("Mesas").active(true).build());
            furnitureRepository.save(Furniture.builder()
                    .name("Mesa Inactiva").price(new BigDecimal("500.00"))
                    .stockQuantity(3).category("Mesas").active(false).build());

            mockMvc.perform(get("/api/v1/muebles")
                            .param("category", "Mesas"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.totalElements").value(2));
        }
    }

    @Nested
    @DisplayName("Stock deduction via Order")
    class StockDeduction {

        @Test
        @DisplayName("should deduct stock when order is created")
        void shouldDeductStock_whenOrderCreated() throws Exception {
            var furniture = furnitureRepository.save(Furniture.builder()
                    .name("Mesa de Roble").description("Mesa maciza")
                    .price(new BigDecimal("1500.00")).woodType("Roble")
                    .category("Mesas").stockQuantity(10).active(true).build());

            var client = clientRepository.save(Client.builder()
                    .name("Test Client").email("test@example.com")
                    .phone("555-0000").address("Test Address").rfc("TEST990101").build());

            var orderBody = objectMapper.writeValueAsString(
                    new com.madera.sys_madera.dto.request.OrderRequest(
                            client.getId(), "Nota de prueba",
                            List.of(new com.madera.sys_madera.dto.request.OrderRequest.OrderDetailRequest(
                                    furniture.getId(), 3))));

            mockMvc.perform(post("/api/v1/pedidos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(orderBody))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.details[0].quantity").value(3))
                    .andExpect(jsonPath("$.details[0].furnitureName").value("Mesa de Roble"))
                    .andExpect(jsonPath("$.totalAmount").value(4500.00));

            var refreshed = furnitureRepository.findById(furniture.getId()).orElseThrow();
            assertThat(refreshed.getStockQuantity()).isEqualTo(7);
        }

        @Test
        @DisplayName("should throw 400 when stock is insufficient")
        void shouldThrow400_whenInsufficientStock() throws Exception {
            var furniture = furnitureRepository.save(Furniture.builder()
                    .name("Silla de Roble").description("Silla")
                    .price(new BigDecimal("500.00")).woodType("Roble")
                    .category("Sillas").stockQuantity(2).active(true).build());

            var client = clientRepository.save(Client.builder()
                    .name("Test Client").email("test2@example.com")
                    .phone("555-0000").address("Test Address").rfc("TEST990102").build());

            var orderBody = objectMapper.writeValueAsString(
                    new com.madera.sys_madera.dto.request.OrderRequest(
                            client.getId(), "Nota",
                            List.of(new com.madera.sys_madera.dto.request.OrderRequest.OrderDetailRequest(
                                    furniture.getId(), 5))));

            mockMvc.perform(post("/api/v1/pedidos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(orderBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(
                            org.hamcrest.Matchers.containsString("Stock insuficiente")));

            var refreshed = furnitureRepository.findById(furniture.getId()).orElseThrow();
            assertThat(refreshed.getStockQuantity()).isEqualTo(2);
        }
    }
}
