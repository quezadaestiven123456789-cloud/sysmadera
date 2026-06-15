package com.madera.sys_madera.security.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DisplayName("SecurityConfig")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("public endpoints")
    class PublicEndpoints {

        @Test
        @DisplayName("should allow access to auth endpoint without authentication")
        void shouldAllowAuthEndpoint() throws Exception {
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Error de validación"));
        }
    }

    @Nested
    @DisplayName("protected endpoints")
    class ProtectedEndpoints {

        @Test
        @DisplayName("should return 401 when accessing protected endpoint without token")
        void shouldReturn401_whenNoToken() throws Exception {
            mockMvc.perform(get("/api/v1/facturas"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("No autorizado. Token inválido o ausente."));
        }

        @Test
        @WithMockUser(roles = "CLIENTE")
        @DisplayName("should return 403 when user has insufficient role")
        void shouldReturn403_whenWrongRole() throws Exception {
            mockMvc.perform(get("/api/v1/facturas"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("No tienes permisos para acceder a este recurso"));
        }
    }

}
