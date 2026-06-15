package com.madera.sys_madera.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtTokenProvider")
class JwtTokenProviderTest {

    private static final String SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970337336763979244226452948404D635166546A576E5A7234753778214125442A47";
    private static final long EXPIRATION_MS = 3600000;
    private static final String USERNAME = "juanperez";
    private static final List<String> ROLES = List.of("ROLE_ADMIN", "ROLE_EMPLEADO");

    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider(SECRET, EXPIRATION_MS);
    }

    @Nested
    @DisplayName("generateToken")
    class GenerateToken {

        @Test
        @DisplayName("should generate a valid JWT token for given username and roles")
        void shouldGenerateToken() {
            String token = tokenProvider.generateToken(USERNAME, ROLES);

            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
            assertThat(token.split("\\.")).hasSize(3);
        }

        @Test
        @DisplayName("should generate different tokens for different usernames")
        void shouldGenerateDifferentTokensForDifferentUsers() {
            String token1 = tokenProvider.generateToken(USERNAME, ROLES);
            String token2 = tokenProvider.generateToken("otro_usuario", List.of("ROLE_CLIENTE"));

            assertThat(token1).isNotEqualTo(token2);
        }

        @Test
        @DisplayName("should embed roles in token claims")
        void shouldEmbedRolesInToken() {
            String token = tokenProvider.generateToken(USERNAME, ROLES);

            List<String> extractedRoles = tokenProvider.getRolesFromToken(token);

            assertThat(extractedRoles).containsExactly("ROLE_ADMIN", "ROLE_EMPLEADO");
        }
    }

    @Nested
    @DisplayName("getUsernameFromToken")
    class GetUsernameFromToken {

        @Test
        @DisplayName("should extract username from valid token")
        void shouldExtractUsername() {
            String token = tokenProvider.generateToken(USERNAME, ROLES);

            String extracted = tokenProvider.getUsernameFromToken(token);

            assertThat(extracted).isEqualTo(USERNAME);
        }

        @Test
        @DisplayName("should extract username with special characters")
        void shouldExtractUsernameWithSpecialChars() {
            String username = "admin_user_123";
            String token = tokenProvider.generateToken(username, ROLES);

            String extracted = tokenProvider.getUsernameFromToken(token);

            assertThat(extracted).isEqualTo(username);
        }
    }

    @Nested
    @DisplayName("getRolesFromToken")
    class GetRolesFromToken {

        @Test
        @DisplayName("should extract roles from valid token")
        void shouldExtractRoles() {
            String token = tokenProvider.generateToken(USERNAME, ROLES);

            List<String> extractedRoles = tokenProvider.getRolesFromToken(token);

            assertThat(extractedRoles).containsExactly("ROLE_ADMIN", "ROLE_EMPLEADO");
        }

        @Test
        @DisplayName("should extract single role from token")
        void shouldExtractSingleRole() {
            List<String> singleRole = List.of("ROLE_CLIENTE");
            String token = tokenProvider.generateToken(USERNAME, singleRole);

            List<String> extractedRoles = tokenProvider.getRolesFromToken(token);

            assertThat(extractedRoles).containsExactly("ROLE_CLIENTE");
        }
    }

    @Nested
    @DisplayName("validateToken")
    class ValidateToken {

        @Test
        @DisplayName("should return true for valid token")
        void shouldReturnTrue_whenValidToken() {
            String token = tokenProvider.generateToken(USERNAME, ROLES);

            boolean valid = tokenProvider.validateToken(token);

            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("should return false for expired token")
        void shouldReturnFalse_whenExpiredToken() {
            SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
            String expiredToken = Jwts.builder()
                    .subject(USERNAME)
                    .claim("roles", ROLES)
                    .issuedAt(new Date(System.currentTimeMillis() - 5000))
                    .expiration(new Date(System.currentTimeMillis() - 1000))
                    .signWith(key)
                    .compact();

            boolean valid = tokenProvider.validateToken(expiredToken);

            assertThat(valid).isFalse();
        }

        @Test
        @DisplayName("should return false for malformed token")
        void shouldReturnFalse_whenMalformedToken() {
            boolean valid = tokenProvider.validateToken("malformed-token-string");

            assertThat(valid).isFalse();
        }

        @Test
        @DisplayName("should return false for token with wrong signature")
        void shouldReturnFalse_whenWrongSignature() {
            String differentSecret = "7336763979244226452948404D635166546A576E5A7234753778214125442A47404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
            SecretKey otherKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(differentSecret));
            String tokenWithDifferentKey = Jwts.builder()
                    .subject(USERNAME)
                    .claim("roles", ROLES)
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                    .signWith(otherKey)
                    .compact();

            boolean valid = tokenProvider.validateToken(tokenWithDifferentKey);

            assertThat(valid).isFalse();
        }

        @Test
        @DisplayName("should return false for empty token")
        void shouldReturnFalse_whenTokenEmpty() {
            boolean valid = tokenProvider.validateToken("");

            assertThat(valid).isFalse();
        }

        @Test
        @DisplayName("should return false for null token")
        void shouldReturnFalse_whenTokenNull() {
            boolean valid = tokenProvider.validateToken(null);

            assertThat(valid).isFalse();
        }

        @Test
        @DisplayName("should return false for token with only signature")
        void shouldReturnFalse_whenOnlySignature() {
            boolean valid = tokenProvider.validateToken("..");

            assertThat(valid).isFalse();
        }
    }

    @Nested
    @DisplayName("comprehensive flow")
    class ComprehensiveFlow {

        @Test
        @DisplayName("should generate, validate, extract username and roles successfully")
        void shouldCompleteFullFlow() {
            String token = tokenProvider.generateToken(USERNAME, ROLES);

            assertThat(tokenProvider.validateToken(token)).isTrue();
            assertThat(tokenProvider.getUsernameFromToken(token)).isEqualTo(USERNAME);
            assertThat(tokenProvider.getRolesFromToken(token)).containsExactly("ROLE_ADMIN", "ROLE_EMPLEADO");
        }
    }
}
