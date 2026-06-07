package com.madera.sys_madera.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.madera.sys_madera.dto.request.OrderRequest;
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
@DisplayName("Order Integration (full stack)")
class OrderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private FurnitureRepository furnitureRepository;

    @Autowired
    private OrderRepository orderRepository;

    private Client savedClient;
    private Furniture savedFurniture;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        furnitureRepository.deleteAll();
        clientRepository.deleteAll();

        savedClient = clientRepository.save(Client.builder()
                .name("Test Client")
                .email("test@example.com")
                .phone("555-0000")
                .address("Test Address")
                .rfc("TEST990101")
                .build());

        savedFurniture = furnitureRepository.save(Furniture.builder()
                .name("Mesa de Roble")
                .description("Mesa de roble macizo")
                .price(new BigDecimal("1500.00"))
                .woodType("Roble")
                .category("Mesas")
                .stockQuantity(10)
                .active(true)
                .build());
    }

    private OrderRequest createRequest(int quantity) {
        return new OrderRequest(
                savedClient.getId(),
                "Nota de prueba",
                List.of(new OrderRequest.OrderDetailRequest(savedFurniture.getId(), quantity))
        );
    }

    @Nested
    @DisplayName("POST /api/v1/pedidos")
    class Create {

        @Test
        @DisplayName("should create order and deduct furniture stock")
        void shouldCreateOrderAndDeductStock() throws Exception {
            var request = createRequest(3);

            mockMvc.perform(post("/api/v1/pedidos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.orderNumber").isNotEmpty())
                    .andExpect(jsonPath("$.status").value("PENDIENTE"))
                    .andExpect(jsonPath("$.totalAmount").value(4500.00))
                    .andExpect(jsonPath("$.clientName").value("Test Client"))
                    .andExpect(jsonPath("$.details[0].furnitureName").value("Mesa de Roble"))
                    .andExpect(jsonPath("$.details[0].quantity").value(3));

            Furniture refreshed = furnitureRepository.findById(savedFurniture.getId()).orElseThrow();
            assertThat(refreshed.getStockQuantity()).isEqualTo(7);
        }

        @Test
        @DisplayName("should throw 400 when stock insufficient")
        void shouldThrow400_whenInsufficientStock() throws Exception {
            var request = createRequest(20);

            mockMvc.perform(post("/api/v1/pedidos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(
                            org.hamcrest.Matchers.containsString("Stock insuficiente")));

            Furniture refreshed = furnitureRepository.findById(savedFurniture.getId()).orElseThrow();
            assertThat(refreshed.getStockQuantity()).isEqualTo(10);
        }

        @Test
        @DisplayName("should throw 400 when details are empty")
        void shouldThrow400_whenDetailsEmpty() throws Exception {
            var request = new OrderRequest(savedClient.getId(), "Nota", List.of());

            mockMvc.perform(post("/api/v1/pedidos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should throw 404 when client not found")
        void shouldThrow404_whenClientNotFound() throws Exception {
            var request = new OrderRequest(999L, "Nota",
                    List.of(new OrderRequest.OrderDetailRequest(savedFurniture.getId(), 1)));

            mockMvc.perform(post("/api/v1/pedidos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(
                            org.hamcrest.Matchers.containsString("Cliente")));
        }

        @Test
        @DisplayName("should throw 404 when furniture not found")
        void shouldThrow404_whenFurnitureNotFound() throws Exception {
            var request = new OrderRequest(savedClient.getId(), "Nota",
                    List.of(new OrderRequest.OrderDetailRequest(999L, 1)));

            mockMvc.perform(post("/api/v1/pedidos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(
                            org.hamcrest.Matchers.containsString("Mueble")));
        }

        @Test
        @DisplayName("should create order with multiple details and calculate correct total")
        void shouldCreateOrderWithMultipleDetails() throws Exception {
            var furniture2 = furnitureRepository.save(Furniture.builder()
                    .name("Silla de Roble")
                    .description("Silla de roble")
                    .price(new BigDecimal("500.00"))
                    .woodType("Roble")
                    .category("Sillas")
                    .stockQuantity(20)
                    .active(true)
                    .build());

            var request = new OrderRequest(
                    savedClient.getId(),
                    "Pedido múltiple",
                    List.of(
                            new OrderRequest.OrderDetailRequest(savedFurniture.getId(), 2),
                            new OrderRequest.OrderDetailRequest(furniture2.getId(), 5)
                    )
            );

            mockMvc.perform(post("/api/v1/pedidos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.totalAmount").value(5500.00))
                    .andExpect(jsonPath("$.details.length()").value(2));

            assertThat(furnitureRepository.findById(savedFurniture.getId()).orElseThrow().getStockQuantity())
                    .isEqualTo(8);
            assertThat(furnitureRepository.findById(furniture2.getId()).orElseThrow().getStockQuantity())
                    .isEqualTo(15);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/pedidos/{id}")
    class FindById {

        @Test
        @DisplayName("should return order when exists")
        void shouldReturnOrder_whenExists() throws Exception {
            var request = createRequest(2);
            var result = mockMvc.perform(post("/api/v1/pedidos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andReturn();

            String orderId = objectMapper.readTree(result.getResponse().getContentAsString())
                    .get("id").asText();

            mockMvc.perform(get("/api/v1/pedidos/{id}", orderId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(orderId))
                    .andExpect(jsonPath("$.status").value("PENDIENTE"))
                    .andExpect(jsonPath("$.clientName").value("Test Client"));
        }

        @Test
        @DisplayName("should throw 404 when order not found")
        void shouldThrow404_whenNotFound() throws Exception {
            mockMvc.perform(get("/api/v1/pedidos/{id}", 999L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(
                            org.hamcrest.Matchers.containsString("Orden")));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/pedidos")
    class FindAll {

        @Test
        @DisplayName("should return paginated orders")
        void shouldReturnPaginatedOrders() throws Exception {
            var request = createRequest(1);
            mockMvc.perform(post("/api/v1/pedidos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            mockMvc.perform(get("/api/v1/pedidos")
                            .param("page", "0")
                            .param("size", "10")
                            .param("sort", "id")
                            .param("direction", "asc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/pedidos/{id}/estado")
    class UpdateStatus {

        @Test
        @DisplayName("should update order status")
        void shouldUpdateStatus() throws Exception {
            var request = createRequest(1);
            var result = mockMvc.perform(post("/api/v1/pedidos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andReturn();

            String orderId = objectMapper.readTree(result.getResponse().getContentAsString())
                    .get("id").asText();

            mockMvc.perform(patch("/api/v1/pedidos/{id}/estado", orderId)
                            .param("status", "COMPLETADO"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("COMPLETADO"));
        }

        @Test
        @DisplayName("should throw 400 when status is invalid")
        void shouldThrow400_whenInvalidStatus() throws Exception {
            var request = createRequest(1);
            var result = mockMvc.perform(post("/api/v1/pedidos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andReturn();

            String orderId = objectMapper.readTree(result.getResponse().getContentAsString())
                    .get("id").asText();

            mockMvc.perform(patch("/api/v1/pedidos/{id}/estado", orderId)
                            .param("status", "INVALIDO"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(
                            org.hamcrest.Matchers.containsString("inválido")));
        }
    }

}
