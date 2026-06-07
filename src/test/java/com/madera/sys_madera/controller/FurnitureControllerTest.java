package com.madera.sys_madera.controller;

import com.madera.sys_madera.dto.request.FurnitureRequest;
import com.madera.sys_madera.dto.response.FurnitureResponse;
import com.madera.sys_madera.dto.response.PagedResponse;
import com.madera.sys_madera.exception.DuplicateResourceException;
import com.madera.sys_madera.exception.ResourceNotFoundException;
import com.madera.sys_madera.security.CustomUserDetailsService;
import com.madera.sys_madera.security.jwt.JwtTokenProvider;
import com.madera.sys_madera.service.FurnitureService;
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

@WebMvcTest(value = FurnitureController.class, excludeAutoConfiguration = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        JpaRepositoriesAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
@Import(FurnitureControllerTest.TestSecurityConfig.class)
@WithMockUser(roles = "ADMIN")
@DisplayName("FurnitureController")
class FurnitureControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {
    }

    private static final String BASE_PATH = "/api/v1/muebles";
    private static final Long FURNITURE_ID = 1L;
    private static final String FURNITURE_NAME = "Mesa Roble";
    private static final BigDecimal PRICE = new BigDecimal("250.00");
    private static final String WOOD_TYPE = "Roble";
    private static final String CATEGORY = "Mesas";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FurnitureService furnitureService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Nested
    @DisplayName("POST /api/v1/muebles")
    class Create {

        @Test
        @DisplayName("should return 201 when furniture is created")
        void shouldReturn201() throws Exception {
            var requestBody = buildJsonRequest();

            given(furnitureService.create(any(FurnitureRequest.class)))
                    .willReturn(buildResponse());

            mockMvc.perform(post(BASE_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(FURNITURE_ID))
                    .andExpect(jsonPath("$.name").value(FURNITURE_NAME))
                    .andExpect(jsonPath("$.price").value(PRICE.doubleValue()))
                    .andExpect(jsonPath("$.category").value(CATEGORY));
        }

        @Test
        @DisplayName("should return 400 when request body is invalid")
        void shouldReturn400_whenInvalidBody() throws Exception {
            var invalidBody = """
                    {
                        "name": ""
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
    @DisplayName("PUT /api/v1/muebles/{id}")
    class Update {

        @Test
        @DisplayName("should return 200 when furniture is updated")
        void shouldReturn200() throws Exception {
            var requestBody = buildJsonRequest();

            var updated = new FurnitureResponse(
                    FURNITURE_ID, "Mesa Roble Premium", "Descripción",
                    PRICE, WOOD_TYPE, "100x200", CATEGORY, 10,
                    true, null, LocalDateTime.now(), LocalDateTime.now());

            given(furnitureService.update(anyLong(), any(FurnitureRequest.class)))
                    .willReturn(updated);

            mockMvc.perform(put(BASE_PATH + "/{id}", FURNITURE_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(FURNITURE_ID))
                    .andExpect(jsonPath("$.name").value("Mesa Roble Premium"));
        }

        @Test
        @DisplayName("should return 404 when furniture not found")
        void shouldReturn404_whenNotFound() throws Exception {
            var requestBody = buildJsonRequest();

            given(furnitureService.update(anyLong(), any(FurnitureRequest.class)))
                    .willThrow(new ResourceNotFoundException("Mueble", "id", 999L));

            mockMvc.perform(put(BASE_PATH + "/{id}", 999L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Mueble no encontrado con id: '999'"));
        }

        @Test
        @DisplayName("should return 409 when name already taken by another furniture")
        void shouldReturn409_whenDuplicateName() throws Exception {
            var requestBody = buildJsonRequest();

            given(furnitureService.update(anyLong(), any(FurnitureRequest.class)))
                    .willThrow(new DuplicateResourceException("El mueble 'Mesa Roble' ya existe"));

            mockMvc.perform(put(BASE_PATH + "/{id}", FURNITURE_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("El mueble 'Mesa Roble' ya existe"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/muebles/{id}")
    class FindById {

        @Test
        @DisplayName("should return 200 when furniture exists")
        void shouldReturn200() throws Exception {
            given(furnitureService.findById(FURNITURE_ID))
                    .willReturn(buildResponse());

            mockMvc.perform(get(BASE_PATH + "/{id}", FURNITURE_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(FURNITURE_ID))
                    .andExpect(jsonPath("$.name").value(FURNITURE_NAME))
                    .andExpect(jsonPath("$.woodType").value(WOOD_TYPE));
        }

        @Test
        @DisplayName("should return 404 when furniture not found")
        void shouldReturn404_whenNotFound() throws Exception {
            given(furnitureService.findById(999L))
                    .willThrow(new ResourceNotFoundException("Mueble", "id", 999L));

            mockMvc.perform(get(BASE_PATH + "/{id}", 999L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Mueble no encontrado con id: '999'"));
        }

        @Test
        @DisplayName("should return 200 when furniture is inactive")
        void shouldReturn200_whenInactive() throws Exception {
            var inactiveResponse = new FurnitureResponse(
                    FURNITURE_ID, FURNITURE_NAME, "Descripción",
                    PRICE, WOOD_TYPE, "100x200", CATEGORY, 10,
                    false, null, LocalDateTime.now(), LocalDateTime.now());

            given(furnitureService.findById(FURNITURE_ID))
                    .willReturn(inactiveResponse);

            mockMvc.perform(get(BASE_PATH + "/{id}", FURNITURE_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.active").value(false));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/muebles")
    class FindAll {

        @Test
        @DisplayName("should return 200 with paginated furniture")
        void shouldReturn200() throws Exception {
            var pagedResponse = new PagedResponse<>(
                    List.of(buildResponse()), 0, 10, 1L, 1, true);

            given(furnitureService.findAll(anyInt(), anyInt(), anyString(), anyString(), isNull(), isNull()))
                    .willReturn(pagedResponse);

            mockMvc.perform(get(BASE_PATH)
                            .param("page", "0")
                            .param("size", "10")
                            .param("sort", "id")
                            .param("direction", "asc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.content[0].name").value(FURNITURE_NAME))
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.totalPages").value(1))
                    .andExpect(jsonPath("$.page").value(0));
        }

        @Test
        @DisplayName("should accept search parameter")
        void shouldAcceptSearch() throws Exception {
            given(furnitureService.findAll(anyInt(), anyInt(), anyString(), anyString(), eq("Mesa"), isNull()))
                    .willReturn(new PagedResponse<>(List.of(), 0, 10, 0L, 0, true));

            mockMvc.perform(get(BASE_PATH)
                            .param("search", "Mesa"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(0))
                    .andExpect(jsonPath("$.totalElements").value(0))
                    .andExpect(jsonPath("$.totalPages").value(0))
                    .andExpect(jsonPath("$.page").value(0));
        }

        @Test
        @DisplayName("should accept category filter")
        void shouldAcceptCategoryFilter() throws Exception {
            given(furnitureService.findAll(anyInt(), anyInt(), anyString(), anyString(), isNull(), eq(CATEGORY)))
                    .willReturn(new PagedResponse<>(List.of(), 0, 10, 0L, 0, true));

            mockMvc.perform(get(BASE_PATH)
                            .param("category", CATEGORY))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(0))
                    .andExpect(jsonPath("$.totalElements").value(0))
                    .andExpect(jsonPath("$.totalPages").value(0))
                    .andExpect(jsonPath("$.page").value(0));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/muebles/{id}")
    class Delete {

        @Test
        @DisplayName("should return 200 when furniture is deleted")
        void shouldReturn200() throws Exception {
            doNothing().when(furnitureService).delete(FURNITURE_ID);

            mockMvc.perform(delete(BASE_PATH + "/{id}", FURNITURE_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Mueble desactivado exitosamente"));
        }

        @Test
        @DisplayName("should return 404 when furniture not found")
        void shouldReturn404_whenNotFound() throws Exception {
            doThrow(new ResourceNotFoundException("Mueble", "id", 999L))
                    .when(furnitureService).delete(999L);

            mockMvc.perform(delete(BASE_PATH + "/{id}", 999L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Mueble no encontrado con id: '999'"));
        }
    }

    @Nested
    @DisplayName("access control")
    class AccessControl {

        @Test
        @WithMockUser(roles = "EMPLEADO")
        @DisplayName("EMPLEADO should access POST")
        void empleadoCanCreate() throws Exception {
            var body = buildJsonRequest();
            given(furnitureService.create(any(FurnitureRequest.class))).willReturn(buildResponse());

            mockMvc.perform(post(BASE_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated());
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
        @DisplayName("CLIENTE should get 403 on DELETE")
        void clienteCannotDelete() throws Exception {
            mockMvc.perform(delete(BASE_PATH + "/{id}", FURNITURE_ID))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "EMPLEADO")
        @DisplayName("EMPLEADO should get 403 on DELETE")
        void empleadoCannotDelete() throws Exception {
            mockMvc.perform(delete(BASE_PATH + "/{id}", FURNITURE_ID))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("ADMIN should access DELETE")
        void adminCanDelete() throws Exception {
            doNothing().when(furnitureService).delete(FURNITURE_ID);
            mockMvc.perform(delete(BASE_PATH + "/{id}", FURNITURE_ID))
                    .andExpect(status().isOk());
        }
    }

    private FurnitureResponse buildResponse() {
        return new FurnitureResponse(
                FURNITURE_ID, FURNITURE_NAME, "Mesa de roble macizo",
                PRICE, WOOD_TYPE, "100x200x75", CATEGORY, 10,
                true, null, LocalDateTime.now(), LocalDateTime.now());
    }

    private String buildJsonRequest() {
        return """
                {
                    "name": "%s",
                    "description": "Mesa de roble macizo",
                    "price": %s,
                    "woodType": "%s",
                    "dimensions": "100x200x75",
                    "category": "%s",
                    "stockQuantity": 10
                }
                """.formatted(FURNITURE_NAME, PRICE, WOOD_TYPE, CATEGORY);
    }
}
