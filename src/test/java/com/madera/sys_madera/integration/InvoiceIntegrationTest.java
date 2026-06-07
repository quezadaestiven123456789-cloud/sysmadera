package com.madera.sys_madera.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.madera.sys_madera.dto.request.OrderRequest;
import com.madera.sys_madera.model.Client;
import com.madera.sys_madera.model.Furniture;
import com.madera.sys_madera.repository.ClientRepository;
import com.madera.sys_madera.repository.FurnitureRepository;
import com.madera.sys_madera.repository.InvoiceRepository;
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
@DisplayName("Invoice Integration (full stack)")
class InvoiceIntegrationTest {

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

    @Autowired
    private InvoiceRepository invoiceRepository;

    private Client savedClient;
    private Furniture savedFurniture;

    @BeforeEach
    void setUp() {
        invoiceRepository.deleteAll();
        orderRepository.deleteAll();
        furnitureRepository.deleteAll();
        clientRepository.deleteAll();

        savedClient = clientRepository.save(Client.builder()
                .name("Carlos López").email("carlos@example.com")
                .phone("555-1000").address("Av. Reforma 456").rfc("CALO850101")
                .build());

        savedFurniture = furnitureRepository.save(Furniture.builder()
                .name("Escritorio").description("Escritorio de caoba")
                .price(new BigDecimal("2500.00")).woodType("Caoba")
                .category("Oficina").stockQuantity(15).active(true)
                .build());
    }

    private Long createOrder() throws Exception {
        var request = new OrderRequest(
                savedClient.getId(), "Pedido para facturación",
                List.of(new OrderRequest.OrderDetailRequest(savedFurniture.getId(), 2)));

        var result = mockMvc.perform(post("/api/v1/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asLong();
    }

    @Nested
    @DisplayName("Full flow: Client -> Order -> Invoice")
    class FullFlow {

        @Test
        @DisplayName("should create invoice from existing order and persist all relationships")
        void shouldCreateInvoiceFromOrder() throws Exception {
            var body = """
                    {
                        "name": "Nuevo Cliente",
                        "email": "nuevo@example.com",
                        "phone": "555-2000",
                        "rfc": "NUEV123456"
                    }
                    """;

            var clientResult = mockMvc.perform(post("/api/v1/clientes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andReturn();
            Long clientId = objectMapper.readTree(clientResult.getResponse().getContentAsString())
                    .get("id").asLong();

            var furnitureBody = """
                    {
                        "name": "Mesa Facturable",
                        "price": 3000.00,
                        "stockQuantity": 10,
                        "category": "Mesas"
                    }
                    """;

            var furnitureResult = mockMvc.perform(post("/api/v1/muebles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(furnitureBody))
                    .andExpect(status().isCreated())
                    .andReturn();
            Long furnitureId = objectMapper.readTree(furnitureResult.getResponse().getContentAsString())
                    .get("id").asLong();

            var orderRequest = new OrderRequest(
                    clientId, "Pedido completo",
                    List.of(new OrderRequest.OrderDetailRequest(furnitureId, 3)));

            var orderResult = mockMvc.perform(post("/api/v1/pedidos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(orderRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.totalAmount").value(9000.00))
                    .andExpect(jsonPath("$.status").value("PENDIENTE"))
                    .andExpect(jsonPath("$.clientName").value("Nuevo Cliente"))
                    .andReturn();
            Long orderId = objectMapper.readTree(orderResult.getResponse().getContentAsString())
                    .get("id").asLong();

            var invoiceBody = """
                    {
                        "orderId": %d,
                        "issueDate": "2026-06-07",
                        "dueDate": "2026-07-07",
                        "notes": "Factura de prueba"
                    }
                    """.formatted(orderId);

            mockMvc.perform(post("/api/v1/facturas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invoiceBody))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.invoiceNumber").isNotEmpty())
                    .andExpect(jsonPath("$.totalAmount").value(9000.00))
                    .andExpect(jsonPath("$.paidAmount").value(0))
                    .andExpect(jsonPath("$.balance").value(9000.00))
                    .andExpect(jsonPath("$.status").value("PENDIENTE"))
                    .andExpect(jsonPath("$.orderId").value(orderId))
                    .andExpect(jsonPath("$.clientName").value("Nuevo Cliente"))
                    .andExpect(jsonPath("$.orderNumber").isNotEmpty());

            var orderFromDb = orderRepository.findById(orderId).orElseThrow();
            assertThat(orderFromDb.getStatus().name()).isEqualTo("PENDIENTE");

            var invoiceFromDb = invoiceRepository.findByOrderId(orderId);
            assertThat(invoiceFromDb).isPresent();
            assertThat(invoiceFromDb.get().getTotalAmount())
                    .isEqualByComparingTo(new BigDecimal("9000.00"));

            var furnitureFromDb = furnitureRepository.findById(furnitureId).orElseThrow();
            assertThat(furnitureFromDb.getStockQuantity()).isEqualTo(7);
        }
    }

    @Nested
    @DisplayName("POST /api/v1/facturas")
    class Create {

        @Test
        @DisplayName("should create invoice with full payment")
        void shouldCreateInvoice_withFullPayment() throws Exception {
            Long orderId = createOrder();

            var body = """
                    {
                        "orderId": %d,
                        "issueDate": "2026-06-07",
                        "dueDate": "2026-07-07",
                        "paidAmount": 5000.00,
                        "notes": "Pagado completo"
                    }
                    """.formatted(orderId);

            mockMvc.perform(post("/api/v1/facturas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("PAGADA"))
                    .andExpect(jsonPath("$.paidAmount").value(5000.00))
                    .andExpect(jsonPath("$.balance").value(0));
        }

        @Test
        @DisplayName("should throw 404 when order does not exist")
        void shouldThrow404_whenOrderNotFound() throws Exception {
            var body = """
                    {
                        "orderId": 999,
                        "issueDate": "2026-06-07",
                        "dueDate": "2026-07-07"
                    }
                    """;

            mockMvc.perform(post("/api/v1/facturas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(
                            org.hamcrest.Matchers.containsString("Orden")));
        }

        @Test
        @DisplayName("should throw 400 when invoice already exists for order")
        void shouldThrow400_whenDuplicateInvoice() throws Exception {
            Long orderId = createOrder();

            var body = """
                    {
                        "orderId": %d,
                        "issueDate": "2026-06-07",
                        "dueDate": "2026-07-07"
                    }
                    """.formatted(orderId);

            mockMvc.perform(post("/api/v1/facturas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated());

            mockMvc.perform(post("/api/v1/facturas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(
                            org.hamcrest.Matchers.containsString("ya tiene una factura")));
        }

        @Test
        @DisplayName("should throw 400 when paid amount exceeds total")
        void shouldThrow400_whenPaymentExceedsTotal() throws Exception {
            Long orderId = createOrder();

            var body = """
                    {
                        "orderId": %d,
                        "issueDate": "2026-06-07",
                        "dueDate": "2026-07-07",
                        "paidAmount": 99999.00
                    }
                    """.formatted(orderId);

            mockMvc.perform(post("/api/v1/facturas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(
                            org.hamcrest.Matchers.containsString("no puede exceder")));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/facturas/{id}")
    class FindById {

        @Test
        @DisplayName("should return invoice when exists")
        void shouldReturnInvoice_whenExists() throws Exception {
            Long orderId = createOrder();
            var body = """
                    {"orderId": %d, "issueDate": "2026-06-07", "dueDate": "2026-07-07"}
                    """.formatted(orderId);

            var result = mockMvc.perform(post("/api/v1/facturas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andReturn();

            Long invoiceId = objectMapper.readTree(result.getResponse().getContentAsString())
                    .get("id").asLong();

            mockMvc.perform(get("/api/v1/facturas/{id}", invoiceId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.invoiceNumber").isNotEmpty())
                    .andExpect(jsonPath("$.totalAmount").value(5000.00))
                    .andExpect(jsonPath("$.orderId").value(orderId))
                    .andExpect(jsonPath("$.clientName").value("Carlos López"));
        }

        @Test
        @DisplayName("should throw 404 when not found")
        void shouldThrow404_whenNotFound() throws Exception {
            mockMvc.perform(get("/api/v1/facturas/{id}", 999L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(
                            org.hamcrest.Matchers.containsString("Factura")));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/facturas/orden/{orderId}")
    class FindByOrderId {

        @Test
        @DisplayName("should return invoice for given order")
        void shouldReturnInvoiceByOrderId() throws Exception {
            Long orderId = createOrder();
            var body = """
                    {"orderId": %d, "issueDate": "2026-06-07", "dueDate": "2026-07-07"}
                    """.formatted(orderId);

            mockMvc.perform(post("/api/v1/facturas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated());

            mockMvc.perform(get("/api/v1/facturas/orden/{orderId}", orderId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orderId").value(orderId));
        }

        @Test
        @DisplayName("should throw 404 when order has no invoice")
        void shouldThrow404_whenNoInvoice() throws Exception {
            Long orderId = createOrder();

            mockMvc.perform(get("/api/v1/facturas/orden/{orderId}", orderId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(
                            org.hamcrest.Matchers.containsString("Factura")));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/facturas")
    class FindAll {

        @Test
        @DisplayName("should return paginated invoices")
        void shouldReturnPaginatedInvoices() throws Exception {
            var orderId1 = createOrder();
            var orderId2 = createOrder();

            var body1 = """
                    {"orderId": %d, "issueDate": "2026-06-07", "dueDate": "2026-07-07"}
                    """.formatted(orderId1);
            var body2 = """
                    {"orderId": %d, "issueDate": "2026-06-07", "dueDate": "2026-07-07"}
                    """.formatted(orderId2);

            mockMvc.perform(post("/api/v1/facturas").contentType(MediaType.APPLICATION_JSON).content(body1));
            mockMvc.perform(post("/api/v1/facturas").contentType(MediaType.APPLICATION_JSON).content(body2));

            mockMvc.perform(get("/api/v1/facturas")
                            .param("page", "0").param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.totalElements").value(2));
        }

        @Test
        @DisplayName("should filter by status")
        void shouldFilterByStatus() throws Exception {
            var orderId = createOrder();
            var body = """
                    {"orderId": %d, "issueDate": "2026-06-07", "dueDate": "2026-07-07", "paidAmount": 5000.00}
                    """.formatted(orderId);
            mockMvc.perform(post("/api/v1/facturas").contentType(MediaType.APPLICATION_JSON).content(body));

            mockMvc.perform(get("/api/v1/facturas")
                            .param("status", "PAGADA"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/facturas/{id}/pago")
    class RegisterPayment {

        @Test
        @DisplayName("should register full payment and mark as PAGADA")
        void shouldRegisterFullPayment() throws Exception {
            Long orderId = createOrder();
            var body = """
                    {"orderId": %d, "issueDate": "2026-06-07", "dueDate": "2026-07-07"}
                    """.formatted(orderId);

            var result = mockMvc.perform(post("/api/v1/facturas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andReturn();

            Long invoiceId = objectMapper.readTree(result.getResponse().getContentAsString())
                    .get("id").asLong();

            mockMvc.perform(post("/api/v1/facturas/{id}/pago", invoiceId)
                            .param("amount", "5000.00"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("PAGADA"))
                    .andExpect(jsonPath("$.paidAmount").value(5000.00))
                    .andExpect(jsonPath("$.balance").value(0));
        }

        @Test
        @DisplayName("should register partial payment and mark as PAGADA_PARCIAL")
        void shouldRegisterPartialPayment() throws Exception {
            Long orderId = createOrder();
            var body = """
                    {"orderId": %d, "issueDate": "2026-06-07", "dueDate": "2026-07-07"}
                    """.formatted(orderId);

            var result = mockMvc.perform(post("/api/v1/facturas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andReturn();

            Long invoiceId = objectMapper.readTree(result.getResponse().getContentAsString())
                    .get("id").asLong();

            mockMvc.perform(post("/api/v1/facturas/{id}/pago", invoiceId)
                            .param("amount", "2000.00"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("PAGADA_PARCIAL"))
                    .andExpect(jsonPath("$.paidAmount").value(2000.00))
                    .andExpect(jsonPath("$.balance").value(3000.00));

            mockMvc.perform(post("/api/v1/facturas/{id}/pago", invoiceId)
                            .param("amount", "3000.00"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("PAGADA"))
                    .andExpect(jsonPath("$.paidAmount").value(5000.00))
                    .andExpect(jsonPath("$.balance").value(0));
        }

        @Test
        @DisplayName("should throw 400 when payment exceeds balance")
        void shouldThrow400_whenPaymentExceedsBalance() throws Exception {
            Long orderId = createOrder();
            var body = """
                    {"orderId": %d, "issueDate": "2026-06-07", "dueDate": "2026-07-07"}
                    """.formatted(orderId);

            var result = mockMvc.perform(post("/api/v1/facturas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andReturn();

            Long invoiceId = objectMapper.readTree(result.getResponse().getContentAsString())
                    .get("id").asLong();

            mockMvc.perform(post("/api/v1/facturas/{id}/pago", invoiceId)
                            .param("amount", "99999.00"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(
                            org.hamcrest.Matchers.containsString("excede el saldo")));
        }

        @Test
        @DisplayName("should throw 400 when invoice is already paid")
        void shouldThrow400_whenAlreadyPaid() throws Exception {
            Long orderId = createOrder();
            var body = """
                    {"orderId": %d, "issueDate": "2026-06-07", "dueDate": "2026-07-07", "paidAmount": 5000.00}
                    """.formatted(orderId);

            var result = mockMvc.perform(post("/api/v1/facturas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andReturn();

            Long invoiceId = objectMapper.readTree(result.getResponse().getContentAsString())
                    .get("id").asLong();

            mockMvc.perform(post("/api/v1/facturas/{id}/pago", invoiceId)
                            .param("amount", "1000.00"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(
                            org.hamcrest.Matchers.containsString("completamente pagada")));
        }

        @Test
        @DisplayName("should throw 404 when invoice not found")
        void shouldThrow404_whenInvoiceNotFound() throws Exception {
            mockMvc.perform(post("/api/v1/facturas/{id}/pago", 999L)
                            .param("amount", "100.00"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(
                            org.hamcrest.Matchers.containsString("Factura")));
        }
    }
}
