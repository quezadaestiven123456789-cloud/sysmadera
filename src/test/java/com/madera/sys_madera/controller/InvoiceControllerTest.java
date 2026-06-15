package com.madera.sys_madera.controller;

import com.madera.sys_madera.config.RateLimitProperties;
import com.madera.sys_madera.dto.request.InvoiceRequest;
import com.madera.sys_madera.dto.response.InvoiceResponse;
import com.madera.sys_madera.dto.response.PagedResponse;
import com.madera.sys_madera.exception.BadRequestException;
import com.madera.sys_madera.exception.ResourceNotFoundException;
import com.madera.sys_madera.security.CustomUserDetailsService;
import com.madera.sys_madera.security.jwt.JwtTokenProvider;
import com.madera.sys_madera.service.InvoiceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = InvoiceController.class, excludeAutoConfiguration = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        JpaRepositoriesAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
@Import(InvoiceControllerTest.TestSecurityConfig.class)
@WithMockUser(roles = "ADMIN")
@DisplayName("InvoiceController")
class InvoiceControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {
    }

    private static final String BASE_PATH = "/api/v1/facturas";
    private static final Long INVOICE_ID = 1L;
    private static final Long ORDER_ID = 1L;
    private static final String INVOICE_NUMBER = "FAC-001";
    private static final String ORDER_NUMBER = "ORD-001";
    private static final String CLIENT_NAME = "Juan Pérez";
    private static final BigDecimal TOTAL = new BigDecimal("1500.00");
    private static final BigDecimal PAID = new BigDecimal("500.00");
    private static final BigDecimal BALANCE = new BigDecimal("1000.00");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InvoiceService invoiceService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private RateLimitProperties rateLimitProperties;

    @Nested
    @DisplayName("POST /api/v1/facturas")
    class Create {

        @Test
        @DisplayName("should return 201 when invoice is created")
        void shouldReturn201() throws Exception {
            var requestBody = buildJsonRequest();

            given(invoiceService.create(any(InvoiceRequest.class)))
                    .willReturn(buildResponse());

            mockMvc.perform(post(BASE_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(INVOICE_ID))
                    .andExpect(jsonPath("$.invoiceNumber").value(INVOICE_NUMBER))
                    .andExpect(jsonPath("$.orderId").value(ORDER_ID))
                    .andExpect(jsonPath("$.totalAmount").value(TOTAL.doubleValue()));
        }

        @Test
        @DisplayName("should return 400 when request body is invalid")
        void shouldReturn400_whenInvalidBody() throws Exception {
            var invalidBody = """
                    {
                        "orderId": null
                    }
                    """;

            mockMvc.perform(post(BASE_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 500 when body is empty")
        void shouldReturn500_whenEmptyBody() throws Exception {
            mockMvc.perform(post(BASE_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(""))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/facturas/{id}")
    class FindById {

        @Test
        @DisplayName("should return 200 when invoice exists")
        void shouldReturn200() throws Exception {
            given(invoiceService.findById(INVOICE_ID))
                    .willReturn(buildResponse());

            mockMvc.perform(get(BASE_PATH + "/{id}", INVOICE_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(INVOICE_ID))
                    .andExpect(jsonPath("$.invoiceNumber").value(INVOICE_NUMBER))
                    .andExpect(jsonPath("$.clientName").value(CLIENT_NAME));
        }

        @Test
        @DisplayName("should return 404 when invoice not found")
        void shouldReturn404_whenNotFound() throws Exception {
            given(invoiceService.findById(999L))
                    .willThrow(new ResourceNotFoundException("Factura", "id", 999L));

            mockMvc.perform(get(BASE_PATH + "/{id}", 999L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Factura no encontrado con id: '999'"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/facturas/orden/{orderId}")
    class FindByOrderId {

        @Test
        @DisplayName("should return 200 when invoice exists for order")
        void shouldReturn200() throws Exception {
            given(invoiceService.findByOrderId(ORDER_ID))
                    .willReturn(buildResponse());

            mockMvc.perform(get(BASE_PATH + "/orden/{orderId}", ORDER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(INVOICE_ID))
                    .andExpect(jsonPath("$.orderId").value(ORDER_ID));
        }

        @Test
        @DisplayName("should return 404 when no invoice for order")
        void shouldReturn404_whenNotFound() throws Exception {
            given(invoiceService.findByOrderId(999L))
                    .willThrow(new ResourceNotFoundException("Factura", "orderId", 999L));

            mockMvc.perform(get(BASE_PATH + "/orden/{orderId}", 999L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Factura no encontrado con orderId: '999'"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/facturas")
    class FindAll {

        @Test
        @DisplayName("should return 200 with paginated invoices")
        void shouldReturn200() throws Exception {
            var pagedResponse = new PagedResponse<>(
                    List.of(buildResponse()), 0, 10, 1L, 1, true);

            given(invoiceService.findAll(anyInt(), anyInt(), anyString(), anyString(), isNull()))
                    .willReturn(pagedResponse);

            mockMvc.perform(get(BASE_PATH)
                            .param("page", "0")
                            .param("size", "10")
                            .param("sort", "id")
                            .param("direction", "asc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.content[0].invoiceNumber").value(INVOICE_NUMBER))
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.totalPages").value(1))
                    .andExpect(jsonPath("$.page").value(0));
        }

        @Test
        @DisplayName("should accept status filter")
        void shouldAcceptStatusFilter() throws Exception {
            given(invoiceService.findAll(anyInt(), anyInt(), anyString(), anyString(), eq("PENDIENTE")))
                    .willReturn(new PagedResponse<>(List.of(), 0, 10, 0L, 0, true));

            mockMvc.perform(get(BASE_PATH)
                            .param("status", "PENDIENTE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(0))
                    .andExpect(jsonPath("$.totalElements").value(0))
                    .andExpect(jsonPath("$.totalPages").value(0))
                    .andExpect(jsonPath("$.page").value(0));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/facturas/{id}/pago")
    class RegisterPayment {

        @Test
        @DisplayName("should return 200 when payment is registered")
        void shouldReturn200() throws Exception {
            var updated = new InvoiceResponse(
                    INVOICE_ID, INVOICE_NUMBER, LocalDate.now(), LocalDate.now().plusDays(30),
                    TOTAL, TOTAL, BigDecimal.ZERO, "PAGADA", null,
                    ORDER_ID, ORDER_NUMBER, CLIENT_NAME,
                    LocalDateTime.now(), LocalDateTime.now());

            given(invoiceService.registerPayment(INVOICE_ID, new BigDecimal("1000.00")))
                    .willReturn(updated);

            mockMvc.perform(post(BASE_PATH + "/{id}/pago", INVOICE_ID)
                            .param("amount", "1000.00"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("PAGADA"))
                    .andExpect(jsonPath("$.paidAmount").value(TOTAL.doubleValue()));
        }

        @Test
        @DisplayName("should return 400 for negative amount")
        void shouldReturn400_whenNegativeAmount() throws Exception {
            var negativeAmount = new BigDecimal("-100.00");
            given(invoiceService.registerPayment(INVOICE_ID, negativeAmount))
                    .willThrow(new BadRequestException("El monto del pago no puede ser negativo"));

            mockMvc.perform(post(BASE_PATH + "/{id}/pago", INVOICE_ID)
                            .param("amount", "-100.00"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("El monto del pago no puede ser negativo"));
        }

        @Test
        @DisplayName("should return 404 when invoice not found")
        void shouldReturn404_whenNotFound() throws Exception {
            given(invoiceService.registerPayment(999L, new BigDecimal("500.00")))
                    .willThrow(new ResourceNotFoundException("Factura", "id", 999L));

            mockMvc.perform(post(BASE_PATH + "/{id}/pago", 999L)
                            .param("amount", "500.00"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Factura no encontrado con id: '999'"));
        }
    }

    @Nested
    @DisplayName("access control")
    class AccessControl {

        @Test
        @WithMockUser(roles = "EMPLEADO")
        @DisplayName("EMPLEADO should access POST")
        void empleadoCanCreate() throws Exception {
            given(invoiceService.create(any(InvoiceRequest.class))).willReturn(buildResponse());
            mockMvc.perform(post(BASE_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(buildJsonRequest()))
                    .andExpect(status().isCreated());
        }

        @Test
        @WithMockUser(roles = "EMPLEADO")
        @DisplayName("EMPLEADO should access GET /{id}")
        void empleadoCanFindById() throws Exception {
            given(invoiceService.findById(INVOICE_ID)).willReturn(buildResponse());
            mockMvc.perform(get(BASE_PATH + "/{id}", INVOICE_ID))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "CLIENTE")
        @DisplayName("CLIENTE should get 403 on POST")
        void clienteCannotCreate() throws Exception {
            mockMvc.perform(post(BASE_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(buildJsonRequest()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "CLIENTE")
        @DisplayName("CLIENTE should get 403 on GET /{id}")
        void clienteCannotFindById() throws Exception {
            mockMvc.perform(get(BASE_PATH + "/{id}", INVOICE_ID))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "CLIENTE")
        @DisplayName("CLIENTE should get 403 on GET /orden/{orderId}")
        void clienteCannotFindByOrderId() throws Exception {
            mockMvc.perform(get(BASE_PATH + "/orden/{orderId}", ORDER_ID))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "CLIENTE")
        @DisplayName("CLIENTE should get 403 on GET /")
        void clienteCannotFindAll() throws Exception {
            mockMvc.perform(get(BASE_PATH))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "CLIENTE")
        @DisplayName("CLIENTE should get 403 on POST /{id}/pago")
        void clienteCannotRegisterPayment() throws Exception {
            mockMvc.perform(post(BASE_PATH + "/{id}/pago", INVOICE_ID)
                            .param("amount", "500.00"))
                    .andExpect(status().isForbidden());
        }
    }

    private InvoiceResponse buildResponse() {
        return new InvoiceResponse(
                INVOICE_ID, INVOICE_NUMBER, LocalDate.now(), LocalDate.now().plusDays(30),
                TOTAL, PAID, BALANCE, "PENDIENTE", null,
                ORDER_ID, ORDER_NUMBER, CLIENT_NAME,
                LocalDateTime.now(), LocalDateTime.now());
    }

    private String buildJsonRequest() {
        return """
                {
                    "orderId": %d,
                    "issueDate": "2025-06-01",
                    "dueDate": "2025-07-01",
                    "paidAmount": %s,
                    "notes": "Pago inicial"
                }
                """.formatted(ORDER_ID, PAID);
    }
}
