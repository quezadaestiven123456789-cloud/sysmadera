package com.madera.sys_madera.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Nested
    @DisplayName("ResourceNotFoundException")
    class ResourceNotFound {

        @Test
        @DisplayName("should return 404 with resource not found message")
        void shouldReturn404() {
            var ex = new ResourceNotFoundException("TestResource", "id", 999L);
            ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleResourceNotFound(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(404);
            assertThat(response.getBody().message()).isEqualTo("TestResource no encontrado con id: '999'");
            assertThat(response.getBody().errors()).isNull();
            assertThat(response.getBody().timestamp()).isNotNull();
        }
    }

    @Nested
    @DisplayName("BadRequestException")
    class BadRequest {

        @Test
        @DisplayName("should return 400 with bad request message")
        void shouldReturn400() {
            var ex = new BadRequestException("Solicitud inválida");
            ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleBadRequest(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(400);
            assertThat(response.getBody().message()).isEqualTo("Solicitud inválida");
            assertThat(response.getBody().errors()).isNull();
        }
    }

    @Nested
    @DisplayName("DuplicateResourceException")
    class Duplicate {

        @Test
        @DisplayName("should return 409 with conflict message")
        void shouldReturn409() {
            var ex = new DuplicateResourceException("El recurso 'test' ya existe");
            ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleDuplicateResource(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(409);
            assertThat(response.getBody().message()).isEqualTo("El recurso 'test' ya existe");
            assertThat(response.getBody().errors()).isNull();
        }
    }

    @Nested
    @DisplayName("BadCredentialsException")
    class BadCredentials {

        @Test
        @DisplayName("should return 401 with generic invalid credentials message")
        void shouldReturn401() {
            var ex = new BadCredentialsException("bad credentials");
            ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleBadCredentials(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(401);
            assertThat(response.getBody().message()).isEqualTo("Credenciales inválidas");
            assertThat(response.getBody().errors()).isNull();
        }
    }

    @Nested
    @DisplayName("AccessDeniedException")
    class AccessDenied {

        @Test
        @DisplayName("should return 403 with access denied message")
        void shouldReturn403() {
            var ex = new AccessDeniedException("Access denied");
            ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleAccessDenied(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(403);
            assertThat(response.getBody().message()).isEqualTo("No tienes permisos para acceder a este recurso");
            assertThat(response.getBody().errors()).isNull();
        }
    }

    @Nested
    @DisplayName("MethodArgumentNotValidException")
    class ValidationError {

        @Test
        @DisplayName("should return 400 with validation error details")
        void shouldReturn400() {
            var ex = createValidationException("name", "must not be blank");

            ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleValidationErrors(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(400);
            assertThat(response.getBody().message()).isEqualTo("Error de validación");
            assertThat(response.getBody().errors()).containsExactly(
                    Map.entry("name", "must not be blank")
            );
            assertThat(response.getBody().timestamp()).isNotNull();
        }

        @Test
        @DisplayName("should include multiple field errors")
        void shouldIncludeMultipleFieldErrors() {
            var ex = createValidationException(
                    "name", "must not be blank",
                    "email", "must not be blank"
            );

            ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleValidationErrors(ex);

            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().errors()).hasSize(2)
                    .containsEntry("name", "must not be blank")
                    .containsEntry("email", "must not be blank");
        }

        private MethodArgumentNotValidException createValidationException(String... fieldAndErrors) {
            var target = new Object();
            var bindingResult = new BeanPropertyBindingResult(target, "target");
            for (int i = 0; i < fieldAndErrors.length; i += 2) {
                bindingResult.addError(new FieldError(
                        "target", fieldAndErrors[i], fieldAndErrors[i + 1]
                ));
            }
            return new MethodArgumentNotValidException(null, bindingResult);
        }
    }

    @Nested
    @DisplayName("AppException")
    class AppExceptionTest {

        @Test
        @DisplayName("should return custom status with message")
        void shouldReturnCustomStatus() {
            var ex = new AppException("Error personalizado", HttpStatus.I_AM_A_TEAPOT);
            ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleAppException(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.I_AM_A_TEAPOT);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(418);
            assertThat(response.getBody().message()).isEqualTo("Error personalizado");
            assertThat(response.getBody().errors()).isNull();
        }
    }

    @Nested
    @DisplayName("generic Exception")
    class GenericError {

        @Test
        @DisplayName("should return 500 with internal error message")
        void shouldReturn500() {
            var ex = new RuntimeException("Error inesperado");
            ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleGenericException(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(500);
            assertThat(response.getBody().message()).isEqualTo("Ha ocurrido un error interno. Contacta al administrador.");
            assertThat(response.getBody().errors()).isNull();
        }
    }
}
