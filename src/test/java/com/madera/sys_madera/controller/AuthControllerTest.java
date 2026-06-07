package com.madera.sys_madera.controller;

import com.madera.sys_madera.dto.request.LoginRequest;
import com.madera.sys_madera.dto.request.RegisterRequest;
import com.madera.sys_madera.dto.response.AuthResponse;
import com.madera.sys_madera.dto.response.MessageResponse;
import com.madera.sys_madera.security.CustomUserDetailsService;
import com.madera.sys_madera.security.jwt.JwtTokenProvider;
import com.madera.sys_madera.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = AuthController.class, excludeAutoConfiguration = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        JpaRepositoriesAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
@Import(AuthControllerTest.TestSecurityConfig.class)
@WithMockUser(roles = "ADMIN")
@DisplayName("AuthController")
class AuthControllerTest {

    @org.springframework.boot.test.context.TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {
    }

    private static final String BASE_PATH = "/api/v1/auth";
    private static final String TOKEN = "eyJhbGciOiJIUzI1NiJ9.token";
    private static final Long USER_ID = 1L;
    private static final String USERNAME = "jperez";
    private static final String EMAIL = "jperez@example.com";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Nested
    @DisplayName("POST /auth/login")
    class Login {

        @Test
        @DisplayName("should return 200 with AuthResponse when credentials are valid")
        void shouldReturn200() throws Exception {
            var authResponse = new AuthResponse(TOKEN, "Bearer", USER_ID, USERNAME, EMAIL, List.of("ROLE_ADMIN"));

            given(authService.login(any(LoginRequest.class))).willReturn(authResponse);

            mockMvc.perform(post(BASE_PATH + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "username": "jperez",
                                        "password": "password123"
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value(TOKEN))
                    .andExpect(jsonPath("$.type").value("Bearer"))
                    .andExpect(jsonPath("$.id").value(USER_ID))
                    .andExpect(jsonPath("$.username").value(USERNAME))
                    .andExpect(jsonPath("$.email").value(EMAIL));
        }

        @ParameterizedTest
        @MethodSource("com.madera.sys_madera.controller.AuthControllerTest#invalidLoginFields")
        @DisplayName("should return 400 when fields are empty")
        void shouldReturn400_whenFieldsEmpty(String username, String password) throws Exception {
            mockMvc.perform(post(BASE_PATH + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "username": "%s",
                                        "password": "%s"
                                    }
                                    """.formatted(username, password)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 500 when request body is missing")
        void shouldReturn500_whenBodyMissing() throws Exception {
            mockMvc.perform(post(BASE_PATH + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(""))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    @DisplayName("POST /auth/register")
    class Register {

        @Test
        @DisplayName("should return 201 with MessageResponse when registration is valid")
        void shouldReturn201() throws Exception {
            var messageResponse = new MessageResponse("Usuario registrado exitosamente");

            given(authService.register(any(RegisterRequest.class))).willReturn(messageResponse);

            mockMvc.perform(post(BASE_PATH + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "username": "jperez",
                                        "email": "jperez@example.com",
                                        "password": "password123",
                                        "firstName": "Juan",
                                        "lastName": "Pérez"
                                    }
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.message").value("Usuario registrado exitosamente"));
        }

        @Test
        @DisplayName("should return 201 when registering with roles")
        void shouldReturn201_withRoles() throws Exception {
            var messageResponse = new MessageResponse("Usuario registrado exitosamente");

            given(authService.register(any(RegisterRequest.class))).willReturn(messageResponse);

            mockMvc.perform(post(BASE_PATH + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "username": "admin",
                                        "email": "admin@example.com",
                                        "password": "password123",
                                        "firstName": "Admin",
                                        "lastName": "Sistema",
                                        "roles": ["ADMIN", "EMPLEADO"]
                                    }
                                    """))
                    .andExpect(status().isCreated());
        }

        @ParameterizedTest
        @MethodSource("com.madera.sys_madera.controller.AuthControllerTest#invalidRegisterFields")
        @DisplayName("should return 400 when fields are invalid")
        void shouldReturn400_whenFieldsInvalid(String username, String email, String password, String firstName) throws Exception {
            mockMvc.perform(post(BASE_PATH + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "username": "%s",
                                        "email": "%s",
                                        "password": "%s",
                                        "firstName": "%s",
                                        "lastName": "User"
                                    }
                                    """.formatted(username, email, password, firstName)))
                    .andExpect(status().isBadRequest());
        }
    }

    static Stream<Arguments> invalidLoginFields() {
        return Stream.of(
                Arguments.of("", "password123"),
                Arguments.of("jperez", "")
        );
    }

    static Stream<Arguments> invalidRegisterFields() {
        return Stream.of(
                Arguments.of("ab", "test@example.com", "password123", "Test"),
                Arguments.of("testuser", "invalid-email", "password123", "Test"),
                Arguments.of("testuser", "test@example.com", "12345", "Test"),
                Arguments.of("testuser", "test@example.com", "password123", "")
        );
    }
}
