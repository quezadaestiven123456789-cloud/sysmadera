package com.madera.sys_madera.integration;

import com.madera.sys_madera.model.WoodInventory;
import com.madera.sys_madera.repository.WoodInventoryRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(roles = "ADMIN")
@DisplayName("WoodInventory Integration (full stack)")
class WoodInventoryIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private WoodInventoryRepository woodInventoryRepository;

        @BeforeEach
        void setUp() {
                woodInventoryRepository.deleteAll();
        }

        @Nested
        @DisplayName("POST /api/v1/inventario-madera")
        class Create {

                @Test
                @DisplayName("should create inventory item and persist in database")
                void shouldCreateInventoryItem() throws Exception {
                        var body = """
                                        {
                                            "woodType": "Caoba",
                                            "quantity": 100.00,
                                            "unit": "m³",
                                            "unitPrice": 1500.00,
                                            "supplier": "Maderas del Sur",
                                            "description": "Caoba centroamericana",
                                            "minimumStock": 20.00
                                        }
                                        """;

                        mockMvc.perform(post("/api/v1/inventario-madera")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(body))
                                        .andExpect(status().isCreated())
                                        .andExpect(jsonPath("$.woodType").value("Caoba"))
                                        .andExpect(jsonPath("$.quantity").value(100.00))
                                        .andExpect(jsonPath("$.unitPrice").value(1500.00))
                                        .andExpect(jsonPath("$.totalValue").value(150000.00))
                                        .andExpect(jsonPath("$.supplier").value("Maderas del Sur"))
                                        .andExpect(jsonPath("$.lowStock").value(false))
                                        .andExpect(jsonPath("$.id").isNumber());

                        assertThat(woodInventoryRepository.findByWoodTypeIgnoreCase("Caoba")).isPresent();
                }

                @Test
                @DisplayName("should throw 409 when wood type already exists")
                void shouldThrow409_whenDuplicateWoodType() throws Exception {
                        woodInventoryRepository.save(WoodInventory.builder()
                                        .woodType("Roble").quantity(new BigDecimal("50.00"))
                                        .unit("m³").unitPrice(new BigDecimal("800.00")).build());

                        var body = """
                                        {
                                            "woodType": "Roble",
                                            "quantity": 30.00,
                                            "unit": "m³",
                                            "unitPrice": 900.00
                                        }
                                        """;

                        mockMvc.perform(post("/api/v1/inventario-madera")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(body))
                                        .andExpect(status().isConflict())
                                        .andExpect(jsonPath("$.message").value(
                                                        org.hamcrest.Matchers.containsString("Roble")));
                }

                @Test
                @DisplayName("should create item with null optional fields")
                void shouldCreateWithNullOptionals() throws Exception {
                        var body = """
                                        {
                                            "woodType": "Pino",
                                            "quantity": 200.00,
                                            "unit": "m³",
                                            "unitPrice": 350.00
                                        }
                                        """;

                        mockMvc.perform(post("/api/v1/inventario-madera")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(body))
                                        .andExpect(status().isCreated())
                                        .andExpect(jsonPath("$.supplier").isEmpty())
                                        .andExpect(jsonPath("$.description").isEmpty())
                                        .andExpect(jsonPath("$.minimumStock").isEmpty());
                }
        }

        @Nested
        @DisplayName("GET /api/v1/inventario-madera/{id}")
        class FindById {

                @Test
                @DisplayName("should return inventory item when exists")
                void shouldReturnItem_whenExists() throws Exception {
                        var saved = woodInventoryRepository.save(WoodInventory.builder()
                                        .woodType("Cedro").quantity(new BigDecimal("80.00"))
                                        .unit("m³").unitPrice(new BigDecimal("1200.00"))
                                        .supplier("Maderas del Norte").build());

                        mockMvc.perform(get("/api/v1/inventario-madera/{id}", saved.getId()))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.woodType").value("Cedro"))
                                        .andExpect(jsonPath("$.supplier").value("Maderas del Norte"));
                }

                @Test
                @DisplayName("should throw 404 when not found")
                void shouldThrow404_whenNotFound() throws Exception {
                        mockMvc.perform(get("/api/v1/inventario-madera/{id}", 999L))
                                        .andExpect(status().isNotFound())
                                        .andExpect(jsonPath("$.message").value(
                                                        org.hamcrest.Matchers.containsString("Inventario")));
                }
        }

        @Nested
        @DisplayName("GET /api/v1/inventario-madera")
        class FindAll {

                @Test
                @DisplayName("should return paginated inventory items")
                void shouldReturnPaginatedItems() throws Exception {
                        woodInventoryRepository.save(WoodInventory.builder()
                                        .woodType("Roble").quantity(new BigDecimal("100.00"))
                                        .unit("m³").unitPrice(new BigDecimal("800.00")).build());
                        woodInventoryRepository.save(WoodInventory.builder()
                                        .woodType("Pino").quantity(new BigDecimal("200.00"))
                                        .unit("m³").unitPrice(new BigDecimal("350.00")).build());

                        mockMvc.perform(get("/api/v1/inventario-madera")
                                        .param("page", "0").param("size", "10"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.content.length()").value(2))
                                        .andExpect(jsonPath("$.totalElements").value(2));
                }

                @Test
                @DisplayName("should filter by search term")
                void shouldFilterBySearch() throws Exception {
                        woodInventoryRepository.save(WoodInventory.builder()
                                        .woodType("Roble").quantity(new BigDecimal("100.00"))
                                        .unit("m³").unitPrice(new BigDecimal("800.00")).build());
                        woodInventoryRepository.save(WoodInventory.builder()
                                        .woodType("Pino").quantity(new BigDecimal("200.00"))
                                        .unit("m³").unitPrice(new BigDecimal("350.00")).build());

                        mockMvc.perform(get("/api/v1/inventario-madera")
                                        .param("search", "Roble"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.content.length()").value(1))
                                        .andExpect(jsonPath("$.content[0].woodType").value("Roble"));
                }
        }

        @Nested
        @DisplayName("PUT /api/v1/inventario-madera/{id}")
        class Update {

                @Test
                @DisplayName("should update item and persist changes")
                void shouldUpdateItem() throws Exception {
                        var saved = woodInventoryRepository.save(WoodInventory.builder()
                                        .woodType("Caoba").quantity(new BigDecimal("50.00"))
                                        .unit("m³").unitPrice(new BigDecimal("1500.00"))
                                        .supplier("Original").build());

                        var body = """
                                        {
                                            "woodType": "Caoba Premium",
                                            "quantity": 75.00,
                                            "unit": "m³",
                                            "unitPrice": 1800.00,
                                            "supplier": "Proveedor Nuevo",
                                            "description": "Actualizado",
                                            "minimumStock": 10.00
                                        }
                                        """;

                        mockMvc.perform(put("/api/v1/inventario-madera/{id}", saved.getId())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(body))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.woodType").value("Caoba Premium"))
                                        .andExpect(jsonPath("$.quantity").value(75.00))
                                        .andExpect(jsonPath("$.unitPrice").value(1800.00))
                                        .andExpect(jsonPath("$.supplier").value("Proveedor Nuevo"))
                                        .andExpect(jsonPath("$.totalValue").value(135000.00));

                        var refreshed = woodInventoryRepository.findById(saved.getId()).orElseThrow();
                        assertThat(refreshed.getQuantity()).isEqualByComparingTo(new BigDecimal("75.00"));
                        assertThat(refreshed.getUnitPrice()).isEqualByComparingTo(new BigDecimal("1800.00"));
                }

                @Test
                @DisplayName("should throw 404 when updating non-existent item")
                void shouldThrow404_whenNotFound() throws Exception {
                        var body = """
                                        {
                                            "woodType": "Inexistente",
                                            "quantity": 10.00,
                                            "unit": "m³",
                                            "unitPrice": 100.00
                                        }
                                        """;

                        mockMvc.perform(put("/api/v1/inventario-madera/{id}", 999L)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(body))
                                        .andExpect(status().isNotFound())
                                        .andExpect(jsonPath("$.message").value(
                                                        org.hamcrest.Matchers.containsString("Inventario")));
                }

                @Test
                @DisplayName("should throw 409 when new wood type conflicts")
                void shouldThrow409_whenWoodTypeConflict() throws Exception {
                        woodInventoryRepository.save(WoodInventory.builder()
                                        .woodType("Roble").quantity(new BigDecimal("100.00"))
                                        .unit("m³").unitPrice(new BigDecimal("800.00")).build());
                        var saved = woodInventoryRepository.save(WoodInventory.builder()
                                        .woodType("Pino").quantity(new BigDecimal("200.00"))
                                        .unit("m³").unitPrice(new BigDecimal("350.00")).build());

                        var body = """
                                        {
                                            "woodType": "Roble",
                                            "quantity": 50.00,
                                            "unit": "m³",
                                            "unitPrice": 900.00
                                        }
                                        """;

                        mockMvc.perform(put("/api/v1/inventario-madera/{id}", saved.getId())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(body))
                                        .andExpect(status().isConflict())
                                        .andExpect(jsonPath("$.message").value(
                                                        org.hamcrest.Matchers.containsString("Roble")));
                }
        }

        @Nested
        @DisplayName("GET /api/v1/inventario-madera/bajo-stock")
        class FindLowStock {

                @Test
                @DisplayName("should return items below minimum stock")
                void shouldReturnLowStockItems() throws Exception {
                        woodInventoryRepository.save(WoodInventory.builder()
                                        .woodType("Roble").quantity(new BigDecimal("30.00"))
                                        .unit("m³").unitPrice(new BigDecimal("800.00"))
                                        .minimumStock(new BigDecimal("50.00")).build());
                        mockMvc.perform(get("/api/v1/inventario-madera/bajo-stock"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.length()").value(1))
                                        .andExpect(jsonPath("$[0].woodType").value("Roble"))
                                        .andExpect(jsonPath("$[0].lowStock").value(true));
                }

                @Test
                @DisplayName("should return empty when no items below minimum")
                void shouldReturnEmpty_whenNoneLow() throws Exception {
                        woodInventoryRepository.save(WoodInventory.builder()
                                        .woodType("Roble").quantity(new BigDecimal("100.00"))
                                        .unit("m³").unitPrice(new BigDecimal("800.00"))
                                        .minimumStock(new BigDecimal("50.00")).build());

                        mockMvc.perform(get("/api/v1/inventario-madera/bajo-stock"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.length()").value(0));
                }
        }

        @Nested
        @DisplayName("DELETE /api/v1/inventario-madera/{id}")
        class Delete {

                @Test
                @DisplayName("should delete inventory item from database")
                void shouldDeleteItem() throws Exception {
                        var saved = woodInventoryRepository.save(WoodInventory.builder()
                                        .woodType("Temporal").quantity(new BigDecimal("10.00"))
                                        .unit("m³").unitPrice(new BigDecimal("500.00")).build());

                        mockMvc.perform(delete("/api/v1/inventario-madera/{id}", saved.getId()))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.message").value(
                                                        "Registro de inventario eliminado exitosamente"));

                        assertThat(woodInventoryRepository.findById(saved.getId())).isEmpty();
                }

                @Test
                @DisplayName("should throw 404 when deleting non-existent item")
                void shouldThrow404_whenNotFound() throws Exception {
                        mockMvc.perform(delete("/api/v1/inventario-madera/{id}", 999L))
                                        .andExpect(status().isNotFound())
                                        .andExpect(jsonPath("$.message").value(
                                                        org.hamcrest.Matchers.containsString("Inventario")));
                }
        }
}
