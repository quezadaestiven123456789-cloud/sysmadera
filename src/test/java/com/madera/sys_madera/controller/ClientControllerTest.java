package com.madera.sys_madera.controller;

import com.madera.sys_madera.config.RateLimitProperties;
import com.madera.sys_madera.dto.request.ClientRequest;
import com.madera.sys_madera.dto.response.ClientResponse;
import com.madera.sys_madera.dto.response.PagedResponse;
import com.madera.sys_madera.exception.DuplicateResourceException;
import com.madera.sys_madera.exception.ResourceNotFoundException;
import com.madera.sys_madera.security.CustomUserDetailsService;
import com.madera.sys_madera.security.jwt.JwtTokenProvider;
import com.madera.sys_madera.service.ClientService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = ClientController.class, excludeAutoConfiguration = {
                DataSourceAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class,
                JpaRepositoriesAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
@Import(ClientControllerTest.TestSecurityConfig.class)
@WithMockUser(roles = "ADMIN")
@DisplayName("ClientController")

class ClientControllerTest {

        @TestConfiguration
        @EnableMethodSecurity
        static class TestSecurityConfig {
        }

        private static final String BASE_PATH = "/api/v1/clientes";
        private static final Long CLIENT_ID = 1L;
        private static final String CLIENT_NAME = "Juan Pérez";
        private static final String CLIENT_EMAIL = "juan@example.com";
        private static final String CLIENT_PHONE = "555-1234";
        private static final String CLIENT_ADDRESS = "Av. Siempre Viva 742";
        private static final String CLIENT_RFC = "JUPE800101";

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private ClientService clientService;

        @MockitoBean
        private JwtTokenProvider jwtTokenProvider;

        @MockitoBean
        private CustomUserDetailsService customUserDetailsService;

        @MockitoBean
        private RateLimitProperties rateLimitProperties;

        @Nested
        @DisplayName("POST /api/v1/clientes")
        class Create {

                @Test
                @DisplayName("should return 201 when client is created")
                void shouldReturn201() throws Exception {
                        var requestBody = buildJsonRequest();

                        given(clientService.create(any(ClientRequest.class)))
                                        .willReturn(buildResponse());

                        mockMvc.perform(post(BASE_PATH)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody))
                                        .andExpect(status().isCreated())
                                        .andExpect(jsonPath("$.id").value(CLIENT_ID))
                                        .andExpect(jsonPath("$.name").value(CLIENT_NAME))
                                        .andExpect(jsonPath("$.email").value(CLIENT_EMAIL));
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
        @DisplayName("GET /api/v1/clientes/{id}")
        class FindById {

                @Test
                @DisplayName("should return 200 when client exists")
                void shouldReturn200() throws Exception {
                        given(clientService.findById(CLIENT_ID))
                                        .willReturn(buildResponse());

                        mockMvc.perform(get(BASE_PATH + "/{id}", CLIENT_ID))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.id").value(CLIENT_ID))
                                        .andExpect(jsonPath("$.name").value(CLIENT_NAME))
                                        .andExpect(jsonPath("$.email").value(CLIENT_EMAIL));
                }

                @Test
                @DisplayName("should return 404 when client not found")
                void shouldReturn404_whenNotFound() throws Exception {
                        given(clientService.findById(999L))
                                        .willThrow(new ResourceNotFoundException("Cliente", "id", 999L));

                        mockMvc.perform(get(BASE_PATH + "/{id}", 999L))
                                        .andExpect(status().isNotFound())
                                        .andExpect(jsonPath("$.message").value("Cliente no encontrado con id: '999'"));
                }
        }

        @Nested
        @DisplayName("GET /api/v1/clientes")
        class FindAll {

                @Test
                @DisplayName("should return 200 with paginated clients")
                void shouldReturn200() throws Exception {
                        var pagedResponse = new PagedResponse<>(
                                        List.of(buildResponse()),
                                        0, 10, 1L, 1, true);

                        given(clientService.findAll(anyInt(), anyInt(), anyString(), anyString(), isNull()))
                                        .willReturn(pagedResponse);

                        mockMvc.perform(get(BASE_PATH)
                                        .param("page", "0")
                                        .param("size", "10")
                                        .param("sort", "id")
                                        .param("direction", "asc"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.content.length()").value(1))
                                        .andExpect(jsonPath("$.content[0].name").value(CLIENT_NAME))
                                        .andExpect(jsonPath("$.totalElements").value(1))
                                        .andExpect(jsonPath("$.totalPages").value(1))
                                        .andExpect(jsonPath("$.page").value(0));
                }

                @Test
                @DisplayName("should accept search parameter")
                void shouldAcceptSearch() throws Exception {
                        given(clientService.findAll(anyInt(), anyInt(), anyString(), anyString(), eq("Juan")))
                                        .willReturn(new PagedResponse<>(List.of(), 0, 10, 0L, 0, true));

                        mockMvc.perform(get(BASE_PATH)
                                        .param("search", "Juan"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.content.length()").value(0))
                                        .andExpect(jsonPath("$.totalElements").value(0))
                                        .andExpect(jsonPath("$.totalPages").value(0))
                                        .andExpect(jsonPath("$.page").value(0));
                }
        }

        @Nested
        @DisplayName("PUT /api/v1/clientes/{id}")
        class Update {

                @Test
                @DisplayName("should return 200 when client is updated")
                void shouldReturn200() throws Exception {
                        var requestBody = buildJsonRequest();

                        var updatedResponse = new ClientResponse(
                                        CLIENT_ID, "Juan Actualizado", CLIENT_EMAIL, "555-9999",
                                        CLIENT_ADDRESS, CLIENT_RFC, null,
                                        LocalDateTime.now(), LocalDateTime.now());

                        given(clientService.update(anyLong(), any(ClientRequest.class)))
                                        .willReturn(updatedResponse);

                        mockMvc.perform(put(BASE_PATH + "/{id}", CLIENT_ID)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.id").value(CLIENT_ID))
                                        .andExpect(jsonPath("$.name").value("Juan Actualizado"))
                                        .andExpect(jsonPath("$.phone").value("555-9999"));
                }

                @Test
                @DisplayName("should return 404 when client not found")
                void shouldReturn404_whenNotFound() throws Exception {
                        var requestBody = buildJsonRequest();

                        given(clientService.update(anyLong(), any(ClientRequest.class)))
                                        .willThrow(new ResourceNotFoundException("Cliente", "id", 999L));

                        mockMvc.perform(put(BASE_PATH + "/{id}", 999L)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody))
                                        .andExpect(status().isNotFound())
                                        .andExpect(jsonPath("$.message").value("Cliente no encontrado con id: '999'"));
                }

                @Test
                @DisplayName("should return 409 when email is already taken")
                void shouldReturn409_whenDuplicateEmail() throws Exception {
                        var requestBody = buildJsonRequest();

                        given(clientService.update(anyLong(), any(ClientRequest.class)))
                                        .willThrow(new DuplicateResourceException("El email 'juan@example.com' ya está registrado"));

                        mockMvc.perform(put(BASE_PATH + "/{id}", CLIENT_ID)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody))
                                        .andExpect(status().isConflict())
                                        .andExpect(jsonPath("$.message").value("El email 'juan@example.com' ya está registrado"));
                }
        }

        @Nested
        @DisplayName("DELETE /api/v1/clientes/{id}")
        class Delete {

                @Test
                @DisplayName("should return 200 when client is deleted")
                void shouldReturn200() throws Exception {
                        doNothing().when(clientService).delete(CLIENT_ID);

                        mockMvc.perform(delete(BASE_PATH + "/{id}", CLIENT_ID))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.message").value("Cliente eliminado exitosamente"));
                }

                @Test
                @DisplayName("should return 404 when client not found")
                void shouldReturn404_whenNotFound() throws Exception {
                        doThrow(new ResourceNotFoundException("Cliente", "id", 999L))
                                        .when(clientService).delete(999L);

                        mockMvc.perform(delete(BASE_PATH + "/{id}", 999L))
                                        .andExpect(status().isNotFound())
                                        .andExpect(jsonPath("$.message").value("Cliente no encontrado con id: '999'"));
                }
        }

        @Nested
        @DisplayName("access control")
        class AccessControl {

                @Test
                @WithMockUser(roles = "EMPLEADO")
                @DisplayName("EMPLEADO should access GET /{id}")
                void empleadoCanFindById() throws Exception {
                        given(clientService.findById(CLIENT_ID)).willReturn(buildResponse());

                        mockMvc.perform(get(BASE_PATH + "/{id}", CLIENT_ID))
                                        .andExpect(status().isOk());
                }

                @Test
                @WithMockUser(roles = "CLIENTE")
                @DisplayName("CLIENTE should access GET /{id}")
                void clienteCanFindById() throws Exception {
                        given(clientService.findById(CLIENT_ID)).willReturn(buildResponse());

                        mockMvc.perform(get(BASE_PATH + "/{id}", CLIENT_ID))
                                        .andExpect(status().isOk());
                }

                @Test
                @WithMockUser(roles = "EMPLEADO")
                @DisplayName("EMPLEADO should access POST")
                void empleadoCanCreate() throws Exception {
                        var body = buildJsonRequest();
                        given(clientService.create(any(ClientRequest.class))).willReturn(buildResponse());

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
                        mockMvc.perform(delete(BASE_PATH + "/{id}", CLIENT_ID))
                                        .andExpect(status().isForbidden());
                }

                @Test
                @WithMockUser(roles = "EMPLEADO")
                @DisplayName("EMPLEADO should get 403 on DELETE")
                void empleadoCannotDelete() throws Exception {
                        mockMvc.perform(delete(BASE_PATH + "/{id}", CLIENT_ID))
                                        .andExpect(status().isForbidden());
                }

                @Test
                @WithMockUser(roles = "ADMIN")
                @DisplayName("ADMIN should access DELETE")
                void adminCanDelete() throws Exception {
                        doNothing().when(clientService).delete(CLIENT_ID);
                        mockMvc.perform(delete(BASE_PATH + "/{id}", CLIENT_ID))
                                        .andExpect(status().isOk());
                }
        }

        private ClientResponse buildResponse() {
                return new ClientResponse(
                                CLIENT_ID, CLIENT_NAME, CLIENT_EMAIL, CLIENT_PHONE,
                                CLIENT_ADDRESS, CLIENT_RFC, null,
                                LocalDateTime.now(), LocalDateTime.now());
        }

        private String buildJsonRequest() {
                return """
                                {
                                    "name": "%s",
                                    "email": "%s",
                                    "phone": "%s",
                                    "address": "%s",
                                    "rfc": "%s"
                                }
                                """.formatted(CLIENT_NAME, CLIENT_EMAIL, CLIENT_PHONE, CLIENT_ADDRESS, CLIENT_RFC);
        }
}
