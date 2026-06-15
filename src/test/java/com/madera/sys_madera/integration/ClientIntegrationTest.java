package com.madera.sys_madera.integration;

import com.madera.sys_madera.repository.ClientRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(roles = "ADMIN")
@DisplayName("Client Integration (full stack)")
class ClientIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ClientRepository clientRepository;

        @BeforeEach
        void setUp() {
                clientRepository.deleteAll();
        }

        @Nested
        @DisplayName("POST /api/v1/clientes")
        class Create {

                @Test
                @DisplayName("should create client and persist in database")
                void shouldCreateClient() throws Exception {
                        var body = """
                                        {
                                            "name": "Juan Pérez",
                                            "email": "juan@example.com",
                                            "phone": "555-1234",
                                            "address": "Calle Principal 123",
                                            "rfc": "JUPE800101"
                                        }
                                        """;

                        mockMvc.perform(post("/api/v1/clientes")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(body))
                                        .andExpect(status().isCreated())
                                        .andExpect(jsonPath("$.name").value("Juan Pérez"))
                                        .andExpect(jsonPath("$.email").value("juan@example.com"))
                                        .andExpect(jsonPath("$.id").isNumber());

                        assertThat(clientRepository.findByEmail("juan@example.com")).isPresent();
                }

                @Test
                @DisplayName("should throw 400 when email already exists")
                void shouldThrow400_whenDuplicateEmail() throws Exception {
                        clientRepository.save(com.madera.sys_madera.model.Client.builder()
                                        .name("Original").email("dup@example.com").build());

                        var body = """
                                        {
                                            "name": "Duplicado",
                                            "email": "dup@example.com",
                                            "phone": "555-0000"
                                        }
                                        """;

                        mockMvc.perform(post("/api/v1/clientes")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(body))
                                        .andExpect(status().isConflict())
                                        .andExpect(jsonPath("$.message").value(
                                                        org.hamcrest.Matchers.containsString("dup@example.com")));
                }

                @Test
                @DisplayName("should throw 400 when RFC already exists")
                void shouldThrow400_whenDuplicateRfc() throws Exception {
                        clientRepository.save(com.madera.sys_madera.model.Client.builder()
                                        .name("Original").email("orig@example.com").rfc("DUPRFC123").build());

                        var body = """
                                        {
                                            "name": "Duplicado",
                                            "email": "otro@example.com",
                                            "rfc": "DUPRFC123"
                                        }
                                        """;

                        mockMvc.perform(post("/api/v1/clientes")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(body))
                                        .andExpect(status().isConflict())
                                        .andExpect(jsonPath("$.message").value(
                                                        org.hamcrest.Matchers.containsString("DUPRFC123")));
                }
        }

        @Nested
        @DisplayName("GET /api/v1/clientes/{id}")
        class FindById {

                @Test
                @DisplayName("should return client when exists")
                void shouldReturnClient_whenExists() throws Exception {
                        var saved = clientRepository.save(com.madera.sys_madera.model.Client.builder()
                                        .name("María García").email("maria@example.com").build());

                        mockMvc.perform(get("/api/v1/clientes/{id}", saved.getId()))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.name").value("María García"))
                                        .andExpect(jsonPath("$.email").value("maria@example.com"));
                }

                @Test
                @DisplayName("should throw 404 when not found")
                void shouldThrow404_whenNotFound() throws Exception {
                        mockMvc.perform(get("/api/v1/clientes/{id}", 999L))
                                        .andExpect(status().isNotFound())
                                        .andExpect(jsonPath("$.message").value(
                                                        org.hamcrest.Matchers.containsString("Cliente")));
                }
        }

        @Nested
        @DisplayName("GET /api/v1/clientes")
        class FindAll {

                @Test
                @DisplayName("should return paginated clients")
                void shouldReturnPaginatedClients() throws Exception {
                        clientRepository.save(com.madera.sys_madera.model.Client.builder()
                                        .name("Cliente A").email("a@example.com").build());
                        clientRepository.save(com.madera.sys_madera.model.Client.builder()
                                        .name("Cliente B").email("b@example.com").build());

                        mockMvc.perform(get("/api/v1/clientes")
                                        .param("page", "0")
                                        .param("size", "10")
                                        .param("sort", "id")
                                        .param("direction", "asc"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.content.length()").value(2))
                                        .andExpect(jsonPath("$.totalElements").value(2));
                }

                @Test
                @DisplayName("should filter by search term")
                void shouldFilterBySearch() throws Exception {
                        clientRepository.save(com.madera.sys_madera.model.Client.builder()
                                        .name("Pedro Infante").email("pedro@example.com").build());
                        clientRepository.save(com.madera.sys_madera.model.Client.builder()
                                        .name("Pedro López").email("lopez@example.com").build());
                        clientRepository.save(com.madera.sys_madera.model.Client.builder()
                                        .name("Ana Pérez").email("ana@example.com").build());

                        mockMvc.perform(get("/api/v1/clientes")
                                        .param("search", "Pedro"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.content.length()").value(2))
                                        .andExpect(jsonPath("$.totalElements").value(2));
                }
        }

        @Nested
        @DisplayName("PUT /api/v1/clientes/{id}")
        class Update {

                @Test
                @DisplayName("should update client and persist changes")
                void shouldUpdateClient() throws Exception {
                        var saved = clientRepository.save(com.madera.sys_madera.model.Client.builder()
                                        .name("Original").email("original@example.com").build());

                        var body = """
                                        {
                                            "name": "Actualizado",
                                            "email": "actualizado@example.com",
                                            "phone": "555-9999"
                                        }
                                        """;

                        mockMvc.perform(put("/api/v1/clientes/{id}", saved.getId())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(body))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.name").value("Actualizado"))
                                        .andExpect(jsonPath("$.email").value("actualizado@example.com"));

                        var refreshed = clientRepository.findById(saved.getId()).orElseThrow();
                        assertThat(refreshed.getName()).isEqualTo("Actualizado");
                        assertThat(refreshed.getPhone()).isEqualTo("555-9999");
                }

                @Test
                @DisplayName("should throw 404 when updating non-existent client")
                void shouldThrow404_whenNotFound() throws Exception {
                        var body = """
                                        {"name": "Nadie", "email": "nadie@example.com"}
                                        """;

                        mockMvc.perform(put("/api/v1/clientes/{id}", 999L)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(body))
                                        .andExpect(status().isNotFound())
                                        .andExpect(jsonPath("$.message").value(
                                                        org.hamcrest.Matchers.containsString("Cliente")));
                }

                @Test
                @DisplayName("should throw 400 when email conflicts with another client")
                void shouldThrow400_whenEmailConflict() throws Exception {
                        clientRepository.save(com.madera.sys_madera.model.Client.builder()
                                        .name("Otro").email("otro@example.com").build());
                        var saved = clientRepository.save(com.madera.sys_madera.model.Client.builder()
                                        .name("Original").email("orig@example.com").build());

                        var body = """
                                        {"name": "Original", "email": "otro@example.com"}
                                        """;

                        mockMvc.perform(put("/api/v1/clientes/{id}", saved.getId())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(body))
                                        .andExpect(status().isConflict())
                                        .andExpect(jsonPath("$.message").value(
                                                        org.hamcrest.Matchers.containsString("otro@example.com")));
                }
        }

        @Nested
        @DisplayName("DELETE /api/v1/clientes/{id}")
        class Delete {

                @Test
                @DisplayName("should delete client from database")
                void shouldDeleteClient() throws Exception {
                        var saved = clientRepository.save(com.madera.sys_madera.model.Client.builder()
                                        .name("Eliminar").email("eliminar@example.com").build());

                        mockMvc.perform(delete("/api/v1/clientes/{id}", saved.getId()))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.message").value("Cliente eliminado exitosamente"));

                        assertThat(clientRepository.findById(saved.getId())).isEmpty();
                }

                @Test
                @DisplayName("should throw 404 when deleting non-existent client")
                void shouldThrow404_whenNotFound() throws Exception {
                        mockMvc.perform(delete("/api/v1/clientes/{id}", 999L))
                                        .andExpect(status().isNotFound())
                                        .andExpect(jsonPath("$.message").value(
                                                        org.hamcrest.Matchers.containsString("Cliente")));
                }
        }
}
