package com.madera.sys_madera.controller;

import com.madera.sys_madera.dto.request.WoodInventoryRequest;
import com.madera.sys_madera.dto.response.PagedResponse;
import com.madera.sys_madera.dto.response.WoodInventoryResponse;
import com.madera.sys_madera.exception.ResourceNotFoundException;
import com.madera.sys_madera.security.CustomUserDetailsService;
import com.madera.sys_madera.security.jwt.JwtTokenProvider;
import com.madera.sys_madera.service.WoodInventoryService;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = WoodInventoryController.class, excludeAutoConfiguration = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        JpaRepositoriesAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
@Import(WoodInventoryControllerTest.TestSecurityConfig.class)
@WithMockUser(roles = "ADMIN")
@DisplayName("WoodInventoryController")
class WoodInventoryControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {
    }

    private static final String BASE_PATH = "/api/v1/inventario-madera";
    private static final Long ID = 1L;
    private static final String WOOD_TYPE = "Roble";
    private static final BigDecimal QUANTITY = new BigDecimal("100.00");
    private static final String UNIT = "m3";
    private static final BigDecimal UNIT_PRICE = new BigDecimal("50.00");
    private static final BigDecimal TOTAL_VALUE = new BigDecimal("5000.00");
    private static final BigDecimal MINIMUM_STOCK = new BigDecimal("10.00");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WoodInventoryService woodInventoryService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Nested
    @DisplayName("POST /api/v1/inventario-madera")
    class Create {

        @Test
        @DisplayName("should return 201 when inventory record is created")
        void shouldReturn201() throws Exception {
            var requestBody = buildJsonRequest();

            given(woodInventoryService.create(any(WoodInventoryRequest.class)))
                    .willReturn(buildResponse());

            mockMvc.perform(post(BASE_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(ID))
                    .andExpect(jsonPath("$.woodType").value(WOOD_TYPE))
                    .andExpect(jsonPath("$.quantity").value(QUANTITY.doubleValue()))
                    .andExpect(jsonPath("$.unit").value(UNIT));
        }

        @Test
        @DisplayName("should return 400 when request body is invalid")
        void shouldReturn400_whenInvalidBody() throws Exception {
            var invalidBody = """
                    {
                        "woodType": ""
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
    @DisplayName("PUT /api/v1/inventario-madera/{id}")
    class Update {

        @Test
        @DisplayName("should return 200 when inventory record is updated")
        void shouldReturn200() throws Exception {
            var requestBody = buildJsonRequest();

            var updated = new WoodInventoryResponse(
                    ID, "Caoba", new BigDecimal("80.00"), UNIT,
                    UNIT_PRICE, new BigDecimal("4000.00"), "Proveedor B",
                    "Caoba premium", MINIMUM_STOCK, false,
                    LocalDateTime.now(), LocalDateTime.now());

            given(woodInventoryService.update(anyLong(), any(WoodInventoryRequest.class)))
                    .willReturn(updated);

            mockMvc.perform(put(BASE_PATH + "/{id}", ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(ID))
                    .andExpect(jsonPath("$.woodType").value("Caoba"));
        }

        @Test
        @DisplayName("should return 404 when inventory record not found")
        void shouldReturn404_whenNotFound() throws Exception {
            var requestBody = buildJsonRequest();

            given(woodInventoryService.update(anyLong(), any(WoodInventoryRequest.class)))
                    .willThrow(new ResourceNotFoundException("Inventario", "id", 999L));

            mockMvc.perform(put(BASE_PATH + "/{id}", 999L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Inventario no encontrado con id: '999'"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/inventario-madera/{id}")
    class FindById {

        @Test
        @DisplayName("should return 200 when record exists")
        void shouldReturn200() throws Exception {
            given(woodInventoryService.findById(ID))
                    .willReturn(buildResponse());

            mockMvc.perform(get(BASE_PATH + "/{id}", ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(ID))
                    .andExpect(jsonPath("$.woodType").value(WOOD_TYPE))
                    .andExpect(jsonPath("$.totalValue").value(TOTAL_VALUE.doubleValue()));
        }

        @Test
        @DisplayName("should return 404 when record not found")
        void shouldReturn404_whenNotFound() throws Exception {
            given(woodInventoryService.findById(999L))
                    .willThrow(new ResourceNotFoundException("Inventario", "id", 999L));

            mockMvc.perform(get(BASE_PATH + "/{id}", 999L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Inventario no encontrado con id: '999'"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/inventario-madera")
    class FindAll {

        @Test
        @DisplayName("should return 200 with paginated records")
        void shouldReturn200() throws Exception {
            var pagedResponse = new PagedResponse<>(
                    List.of(buildResponse()), 0, 10, 1L, 1, true);

            given(woodInventoryService.findAll(anyInt(), anyInt(), anyString(), anyString(), isNull()))
                    .willReturn(pagedResponse);

            mockMvc.perform(get(BASE_PATH)
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.content[0].woodType").value(WOOD_TYPE))
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.totalPages").value(1))
                    .andExpect(jsonPath("$.page").value(0));
        }

        @Test
        @DisplayName("should accept search parameter")
        void shouldAcceptSearch() throws Exception {
            given(woodInventoryService.findAll(anyInt(), anyInt(), anyString(), anyString(), eq("Roble")))
                    .willReturn(new PagedResponse<>(List.of(), 0, 10, 0L, 0, true));

            mockMvc.perform(get(BASE_PATH)
                            .param("search", "Roble"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(0))
                    .andExpect(jsonPath("$.totalElements").value(0))
                    .andExpect(jsonPath("$.totalPages").value(0))
                    .andExpect(jsonPath("$.page").value(0));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/inventario-madera/bajo-stock")
    class FindLowStock {

        @Test
        @DisplayName("should return 200 with low stock items")
        void shouldReturn200() throws Exception {
            var lowStockItem = new WoodInventoryResponse(
                    ID, WOOD_TYPE, new BigDecimal("5.00"), UNIT,
                    UNIT_PRICE, new BigDecimal("250.00"), "Proveedor A",
                    null, MINIMUM_STOCK, true,
                    LocalDateTime.now(), LocalDateTime.now());

            given(woodInventoryService.findLowStock())
                    .willReturn(List.of(lowStockItem));

            mockMvc.perform(get(BASE_PATH + "/bajo-stock"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(ID))
                    .andExpect(jsonPath("$[0].lowStock").value(true));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/inventario-madera/{id}")
    class Delete {

        @Test
        @DisplayName("should return 200 when record is deleted")
        void shouldReturn200() throws Exception {
            doNothing().when(woodInventoryService).delete(ID);

            mockMvc.perform(delete(BASE_PATH + "/{id}", ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Registro de inventario eliminado exitosamente"));
        }

        @Test
        @DisplayName("should return 404 when record not found")
        void shouldReturn404_whenNotFound() throws Exception {
            doThrow(new ResourceNotFoundException("Inventario", "id", 999L))
                    .when(woodInventoryService).delete(999L);

            mockMvc.perform(delete(BASE_PATH + "/{id}", 999L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Inventario no encontrado con id: '999'"));
        }
    }

    @Nested
    @DisplayName("access control")
    class AccessControl {

        @Test
        @WithMockUser(roles = "EMPLEADO")
        @DisplayName("EMPLEADO should access POST")
        void empleadoCanCreate() throws Exception {
            given(woodInventoryService.create(any(WoodInventoryRequest.class))).willReturn(buildResponse());
            mockMvc.perform(post(BASE_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(buildJsonRequest()))
                    .andExpect(status().isCreated());
        }

        @Test
        @WithMockUser(roles = "EMPLEADO")
        @DisplayName("EMPLEADO should access GET /{id}")
        void empleadoCanFindById() throws Exception {
            given(woodInventoryService.findById(ID)).willReturn(buildResponse());
            mockMvc.perform(get(BASE_PATH + "/{id}", ID))
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
            mockMvc.perform(get(BASE_PATH + "/{id}", ID))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "CLIENTE")
        @DisplayName("CLIENTE should get 403 on DELETE")
        void clienteCannotDelete() throws Exception {
            mockMvc.perform(delete(BASE_PATH + "/{id}", ID))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "EMPLEADO")
        @DisplayName("EMPLEADO should get 403 on DELETE")
        void empleadoCannotDelete() throws Exception {
            mockMvc.perform(delete(BASE_PATH + "/{id}", ID))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("ADMIN should access DELETE")
        void adminCanDelete() throws Exception {
            doNothing().when(woodInventoryService).delete(ID);
            mockMvc.perform(delete(BASE_PATH + "/{id}", ID))
                    .andExpect(status().isOk());
        }
    }

    private WoodInventoryResponse buildResponse() {
        return new WoodInventoryResponse(
                ID, WOOD_TYPE, QUANTITY, UNIT, UNIT_PRICE, TOTAL_VALUE,
                "Proveedor A", "Roble macizo", MINIMUM_STOCK, false,
                LocalDateTime.now(), LocalDateTime.now());
    }

    private String buildJsonRequest() {
        return """
                {
                    "woodType": "%s",
                    "quantity": %s,
                    "unit": "%s",
                    "unitPrice": %s,
                    "supplier": "Proveedor A",
                    "description": "Roble macizo",
                    "minimumStock": %s
                }
                """.formatted(WOOD_TYPE, QUANTITY, UNIT, UNIT_PRICE, MINIMUM_STOCK);
    }
}
