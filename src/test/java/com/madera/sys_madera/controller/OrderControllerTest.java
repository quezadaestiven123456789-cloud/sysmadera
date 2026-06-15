package com.madera.sys_madera.controller;

import com.madera.sys_madera.config.RateLimitProperties;
import com.madera.sys_madera.dto.request.OrderRequest;
import com.madera.sys_madera.dto.response.OrderResponse;
import com.madera.sys_madera.dto.response.PagedResponse;
import com.madera.sys_madera.exception.BadRequestException;
import com.madera.sys_madera.exception.ResourceNotFoundException;
import com.madera.sys_madera.security.CustomUserDetailsService;
import com.madera.sys_madera.security.jwt.JwtTokenProvider;
import com.madera.sys_madera.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = OrderController.class, excludeAutoConfiguration = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        JpaRepositoriesAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
@Import(OrderControllerTest.TestSecurityConfig.class)
@WithMockUser(roles = "ADMIN")
@DisplayName("OrderController")
class OrderControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {
    }

    private static final String BASE_PATH = "/api/v1/pedidos";
    private static final Long ORDER_ID = 1L;
    private static final Long CLIENT_ID = 1L;
    private static final Long FURNITURE_ID = 1L;
    private static final String CLIENT_NAME = "Juan Pérez";
    private static final String FURNITURE_NAME = "Mesa Roble";
    private static final BigDecimal TOTAL = new BigDecimal("500.00");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private RateLimitProperties rateLimitProperties;

    @Nested
    @DisplayName("POST /api/v1/pedidos")
    class Create {

        @Test
        @DisplayName("should return 201 when order is created")
        void shouldReturn201() throws Exception {
            var requestBody = buildJsonRequest();

            given(orderService.create(any(OrderRequest.class)))
                    .willReturn(buildResponse());

            mockMvc.perform(post(BASE_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(ORDER_ID))
                    .andExpect(jsonPath("$.clientName").value(CLIENT_NAME))
                    .andExpect(jsonPath("$.totalAmount").value(TOTAL.doubleValue()))
                    .andExpect(jsonPath("$.status").value("PENDIENTE"))
                    .andExpect(jsonPath("$.details[0].furnitureName").value(FURNITURE_NAME));
        }

        @Test
        @DisplayName("should return 400 when request body is invalid")
        void shouldReturn400_whenInvalidBody() throws Exception {
            var invalidBody = """
                    {
                        "clientId": null,
                        "details": []
                    }
                    """;

            mockMvc.perform(post(BASE_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidBody))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/pedidos/{id}")
    class FindById {

        @Test
        @DisplayName("should return 200 when order exists")
        void shouldReturn200() throws Exception {
            given(orderService.findById(ORDER_ID))
                    .willReturn(buildResponse());

            mockMvc.perform(get(BASE_PATH + "/{id}", ORDER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(ORDER_ID))
                    .andExpect(jsonPath("$.clientName").value(CLIENT_NAME))
                    .andExpect(jsonPath("$.status").value("PENDIENTE"));
        }

        @Test
        @DisplayName("should return 404 when order not found")
        void shouldReturn404_whenNotFound() throws Exception {
            given(orderService.findById(999L))
                    .willThrow(new ResourceNotFoundException("Orden", "id", 999L));

            mockMvc.perform(get(BASE_PATH + "/{id}", 999L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Orden no encontrado con id: '999'"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/pedidos")
    class FindAll {

        @Test
        @DisplayName("should return 200 with paginated orders")
        void shouldReturn200() throws Exception {
            given(orderService.findAll(anyInt(), anyInt(), anyString(), anyString(), isNull()))
                    .willReturn(new PagedResponse<>(List.of(buildResponse()), 0, 10, 1L, 1, true));

            mockMvc.perform(get(BASE_PATH)
                            .param("page", "0")
                            .param("size", "10")
                            .param("sort", "id")
                            .param("direction", "asc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.content[0].clientName").value(CLIENT_NAME))
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.totalPages").value(1))
                    .andExpect(jsonPath("$.page").value(0));
        }

        @Test
        @DisplayName("should accept status filter parameter")
        void shouldAcceptStatusFilter() throws Exception {
            given(orderService.findAll(anyInt(), anyInt(), anyString(), anyString(), eq("PENDIENTE")))
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
    @DisplayName("GET /api/v1/pedidos/cliente/{clientId}")
    class FindByClientId {

        @Test
        @DisplayName("should return 200 with client orders")
        void shouldReturn200() throws Exception {
            given(orderService.findByClientId(eq(CLIENT_ID), anyInt(), anyInt(), anyString(), anyString()))
                    .willReturn(new PagedResponse<>(List.of(buildResponse()), 0, 10, 1L, 1, true));

            mockMvc.perform(get(BASE_PATH + "/cliente/{clientId}", CLIENT_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.content[0].clientName").value(CLIENT_NAME))
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.totalPages").value(1))
                    .andExpect(jsonPath("$.page").value(0));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/pedidos/{id}/estado")
    class UpdateStatus {

        @Test
        @DisplayName("should return 200 when status is updated")
        void shouldReturn200() throws Exception {
            var updated = new OrderResponse(
                    ORDER_ID, "ORD-20250601-0001", "COMPLETADO", TOTAL,
                    "Nota", CLIENT_NAME, CLIENT_ID, List.of(),
                    LocalDateTime.now(), LocalDateTime.now());

            given(orderService.updateStatus(anyLong(), eq("COMPLETADO")))
                    .willReturn(updated);

            mockMvc.perform(patch(BASE_PATH + "/{id}/estado", ORDER_ID)
                            .param("status", "COMPLETADO"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("COMPLETADO"));
        }

        @Test
        @DisplayName("should return 400 when status is invalid")
        void shouldReturn400_whenInvalidStatus() throws Exception {
            given(orderService.updateStatus(anyLong(), eq("INVALIDO")))
                    .willThrow(new BadRequestException("Estado inválido: INVALIDO"));

            mockMvc.perform(patch(BASE_PATH + "/{id}/estado", ORDER_ID)
                            .param("status", "INVALIDO"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Estado inválido: INVALIDO"));
        }

        @Test
        @DisplayName("should return 404 when order not found")
        void shouldReturn404_whenNotFound() throws Exception {
            given(orderService.updateStatus(anyLong(), anyString()))
                    .willThrow(new ResourceNotFoundException("Orden", "id", 999L));

            mockMvc.perform(patch(BASE_PATH + "/{id}/estado", 999L)
                            .param("status", "COMPLETADO"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Orden no encontrado con id: '999'"));
        }
    }

    @Nested
    @DisplayName("access control")
    class AccessControl {

        @Test
        @WithMockUser(roles = "EMPLEADO")
        @DisplayName("EMPLEADO should access GET /{id}")
        void empleadoCanFindById() throws Exception {
            given(orderService.findById(ORDER_ID)).willReturn(buildResponse());

            mockMvc.perform(get(BASE_PATH + "/{id}", ORDER_ID))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "CLIENTE")
        @DisplayName("CLIENTE should access GET /{id}")
        void clienteCanFindById() throws Exception {
            given(orderService.findById(ORDER_ID)).willReturn(buildResponse());

            mockMvc.perform(get(BASE_PATH + "/{id}", ORDER_ID))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "EMPLEADO")
        @DisplayName("EMPLEADO should access POST")
        void empleadoCanCreate() throws Exception {
            var body = buildJsonRequest();
            given(orderService.create(any(OrderRequest.class))).willReturn(buildResponse());

            mockMvc.perform(post(BASE_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated());
        }

        @Test
        @WithMockUser(roles = "CLIENTE")
        @DisplayName("CLIENTE should get 403 on GET /cliente/{clientId}")
        void clienteCannotFindByClientId() throws Exception {
            mockMvc.perform(get(BASE_PATH + "/cliente/{clientId}", CLIENT_ID))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "EMPLEADO")
        @DisplayName("EMPLEADO should access PATCH /{id}/estado")
        void empleadoCanUpdateStatus() throws Exception {
            given(orderService.updateStatus(anyLong(), eq("COMPLETADO")))
                    .willReturn(buildResponse());

            mockMvc.perform(patch(BASE_PATH + "/{id}/estado", ORDER_ID)
                            .param("status", "COMPLETADO"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "CLIENTE")
        @DisplayName("CLIENTE should get 403 on POST")
        void clienteCannotCreate() throws Exception {
            var body = buildJsonRequest();
            mockMvc.perform(post(BASE_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "CLIENTE")
        @DisplayName("CLIENTE should get 403 on PATCH /{id}/estado")
        void clienteCannotUpdateStatus() throws Exception {
            mockMvc.perform(patch(BASE_PATH + "/{id}/estado", ORDER_ID)
                            .param("status", "COMPLETADO"))
                    .andExpect(status().isForbidden());
        }
    }

    private OrderResponse buildResponse() {
        var detail = new OrderResponse.OrderDetailResponse(
                1L, FURNITURE_ID, FURNITURE_NAME, 2,
                new BigDecimal("250.00"), new BigDecimal("500.00"));

        return new OrderResponse(
                ORDER_ID, "ORD-20250601-0001", "PENDIENTE", TOTAL,
                "Nota del pedido", CLIENT_NAME, CLIENT_ID,
                List.of(detail), LocalDateTime.now(), LocalDateTime.now());
    }

    private String buildJsonRequest() {
        return """
                {
                    "clientId": %d,
                    "notes": "Nota del pedido",
                    "details": [
                        {
                            "furnitureId": %d,
                            "quantity": 2
                        }
                    ]
                }
                """.formatted(CLIENT_ID, FURNITURE_ID);
    }
}
