package com.madera.sys_madera.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    private static final String TOKEN = "valid.jwt.token";
    private static final String USERNAME = "juanperez";
    private static final List<String> ROLES = List.of("ROLE_ADMIN");

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("doFilterInternal")
    class DoFilterInternal {

        @Test
        @DisplayName("should set authentication with roles from token")
        void shouldSetAuth_whenValidToken() throws Exception {
            given(request.getHeader("Authorization")).willReturn("Bearer " + TOKEN);
            given(jwtTokenProvider.validateToken(TOKEN)).willReturn(true);
            given(jwtTokenProvider.getUsernameFromToken(TOKEN)).willReturn(USERNAME);
            given(jwtTokenProvider.getRolesFromToken(TOKEN)).willReturn(ROLES);

            filter.doFilterInternal(request, response, filterChain);

            var authentication = SecurityContextHolder.getContext().getAuthentication();
            assertThat(authentication).isNotNull();
            assertThat(authentication.getName()).isEqualTo(USERNAME);
            assertThat(authentication.getAuthorities())
                    .extracting("authority")
                    .containsExactly("ROLE_ADMIN");
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("should set authentication with multiple roles from token")
        void shouldSetAuth_whenMultipleRoles() throws Exception {
            var multipleRoles = List.of("ROLE_ADMIN", "ROLE_EMPLEADO");
            given(request.getHeader("Authorization")).willReturn("Bearer " + TOKEN);
            given(jwtTokenProvider.validateToken(TOKEN)).willReturn(true);
            given(jwtTokenProvider.getUsernameFromToken(TOKEN)).willReturn(USERNAME);
            given(jwtTokenProvider.getRolesFromToken(TOKEN)).willReturn(multipleRoles);

            filter.doFilterInternal(request, response, filterChain);

            var authentication = SecurityContextHolder.getContext().getAuthentication();
            assertThat(authentication).isNotNull();
            assertThat(authentication.getAuthorities())
                    .extracting("authority")
                    .containsExactly("ROLE_ADMIN", "ROLE_EMPLEADO");
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("should not set authentication when no token")
        void shouldNotSetAuth_whenNoToken() throws Exception {
            given(request.getHeader("Authorization")).willReturn(null);

            filter.doFilterInternal(request, response, filterChain);

            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("should not set authentication when token is invalid")
        void shouldNotSetAuth_whenInvalidToken() throws Exception {
            given(request.getHeader("Authorization")).willReturn("Bearer " + TOKEN);
            given(jwtTokenProvider.validateToken(TOKEN)).willReturn(false);

            filter.doFilterInternal(request, response, filterChain);

            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("should not set authentication when Authorization header has no Bearer prefix")
        void shouldNotSetAuth_whenNoBearerPrefix() throws Exception {
            given(request.getHeader("Authorization")).willReturn(TOKEN);

            filter.doFilterInternal(request, response, filterChain);

            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("should not set authentication when Authorization header is empty")
        void shouldNotSetAuth_whenEmptyHeader() throws Exception {
            given(request.getHeader("Authorization")).willReturn("");

            filter.doFilterInternal(request, response, filterChain);

            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            verify(filterChain).doFilter(request, response);
        }
    }

}
