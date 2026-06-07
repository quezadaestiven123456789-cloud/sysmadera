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

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtTokenProvider")
class JwtTokenProviderTest {

    private static final String SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970337336763979244226452948404D635166546A576E5A7234753778214125442A47";
    private static final long EXPIRATION_MS = 3600000;
    private static final String USERNAME = "juanperez";

    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider(SECRET, EXPIRATION_MS);
    }

    @Nested
    @DisplayName("generateToken")
    class GenerateToken {

        @Test
        @DisplayName("should generate a valid JWT token for given username")
        void shouldGenerateToken() {
            String token = tokenProvider.generateToken(USERNAME);

            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
            assertThat(token.split("\\.")).hasSize(3);
        }

        @Test
        @DisplayName("should generate different tokens for different usernames")
        void shouldGenerateDifferentTokensForDifferentUsers() {
            String token1 = tokenProvider.generateToken(USERNAME);
            String token2 = tokenProvider.generateToken("otro_usuario");

            assertThat(token1).isNotEqualTo(token2);
        }
    }

    @Nested
    @DisplayName("getUsernameFromToken")
    class GetUsernameFromToken {

        @Test
        @DisplayName("should extract username from valid token")
        void shouldExtractUsername() {
            String token = tokenProvider.generateToken(USERNAME);

            String extracted = tokenProvider.getUsernameFromToken(token);

            assertThat(extracted).isEqualTo(USERNAME);
        }

        @Test
        @DisplayName("should extract username with special characters")
        void shouldExtractUsernameWithSpecialChars() {
            String username = "admin_user_123";
            String token = tokenProvider.generateToken(username);

            String extracted = tokenProvider.getUsernameFromToken(token);

            assertThat(extracted).isEqualTo(username);
        }
    }

    @Nested
    @DisplayName("validateToken")
    class ValidateToken {

        @Test
        @DisplayName("should return true for valid token")
        void shouldReturnTrue_whenValidToken() {
            String token = tokenProvider.generateToken(USERNAME);

            boolean valid = tokenProvider.validateToken(token);

            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("should return false for expired token")
        void shouldReturnFalse_whenExpiredToken() {
            SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
            String expiredToken = Jwts.builder()
                    .subject(USERNAME)
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
        @DisplayName("should generate, validate and extract username successfully")
        void shouldCompleteFullFlow() {
            String token = tokenProvider.generateToken(USERNAME);

            assertThat(tokenProvider.validateToken(token)).isTrue();
            assertThat(tokenProvider.getUsernameFromToken(token)).isEqualTo(USERNAME);
        }
    }
}
