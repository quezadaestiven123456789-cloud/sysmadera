package com.madera.sys_madera.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.madera.sys_madera.exception.GlobalExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.AuthenticationException;

import java.io.PrintWriter;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtEntryPoint")
class JwtEntryPointTest {

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private JwtEntryPoint jwtEntryPoint;

    @Test
    @DisplayName("should return 401 with JSON error body")
    void shouldReturn401() throws Exception {
        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);
        var authException = mock(AuthenticationException.class);
        var writer = mock(PrintWriter.class);

        when(response.getWriter()).thenReturn(writer);

        jwtEntryPoint.commence(request, response, authException);

        verify(response).setContentType("application/json");
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(objectMapper).writeValue(eq(writer), any(GlobalExceptionHandler.ErrorResponse.class));
    }

}
