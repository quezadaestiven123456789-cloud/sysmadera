package com.madera.sys_madera.service.impl;

import com.madera.sys_madera.dto.request.LoginRequest;
import com.madera.sys_madera.dto.request.RegisterRequest;
import com.madera.sys_madera.dto.response.AuthResponse;
import com.madera.sys_madera.dto.response.MessageResponse;
import com.madera.sys_madera.exception.BadRequestException;
import com.madera.sys_madera.exception.DuplicateResourceException;
import com.madera.sys_madera.model.ERole;
import com.madera.sys_madera.model.Role;
import com.madera.sys_madera.model.User;
import com.madera.sys_madera.repository.RoleRepository;
import com.madera.sys_madera.repository.UserRepository;
import com.madera.sys_madera.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl")
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    private static final String USERNAME = "jperez";
    private static final String PASSWORD = "password123";
    private static final String ENCODED_PASSWORD = "$2a$10$encoded";
    private static final String EMAIL = "jperez@example.com";
    private static final String FIRST_NAME = "Juan";
    private static final String LAST_NAME = "Pérez";
    private static final Long USER_ID = 1L;
    private static final String TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";

    @Nested
    @DisplayName("login")
    class Login {

        @Test
        @DisplayName("should return AuthResponse when credentials are valid")
        void shouldReturnAuthResponse_whenValidCredentials() {
            var request = new LoginRequest(USERNAME, PASSWORD);
            var user = buildUser();
            var authentication = givenAuthentication(USERNAME, "ROLE_ADMIN");

            given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .willReturn(authentication);
            given(jwtTokenProvider.generateToken(USERNAME, List.of("ROLE_ADMIN"))).willReturn(TOKEN);
            given(userRepository.findByUsername(USERNAME)).willReturn(Optional.of(user));

            AuthResponse response = authService.login(request);

            assertThat(response.token()).isEqualTo(TOKEN);
            assertThat(response.type()).isEqualTo("Bearer");
            assertThat(response.id()).isEqualTo(USER_ID);
            assertThat(response.username()).isEqualTo(USERNAME);
            assertThat(response.email()).isEqualTo(EMAIL);
            assertThat(response.roles()).containsExactly("ROLE_ADMIN");
        }

        @Test
        @DisplayName("should throw BadCredentialsException when password is wrong")
        void shouldThrowException_whenInvalidCredentials() {
            var request = new LoginRequest(USERNAME, "wrongpassword");

            given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .willThrow(new BadCredentialsException("Bad credentials"));

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BadCredentialsException.class);
        }

        @Test
        @DisplayName("should throw exception when authenticated user not found in database")
        void shouldThrowException_whenUserNotFoundAfterAuth() {
            var request = new LoginRequest(USERNAME, PASSWORD);
            var authentication = givenAuthentication(USERNAME, "ROLE_ADMIN");

            given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .willReturn(authentication);
            given(jwtTokenProvider.generateToken(USERNAME, List.of("ROLE_ADMIN"))).willReturn(TOKEN);
            given(userRepository.findByUsername(USERNAME)).willReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("register")
    class Register {

        @Test
        @DisplayName("should register user with default CLIENTE role when roles are null")
        void shouldRegisterWithDefaultRole_whenRolesNull() {
            var request = new RegisterRequest(USERNAME, EMAIL, PASSWORD, FIRST_NAME, LAST_NAME, null);
            var roleCliente = Role.builder().id(3L).name(ERole.ROLE_CLIENTE).build();
            var savedUser = buildUserWithRoles(Set.of(roleCliente));

            given(userRepository.existsByUsername(USERNAME)).willReturn(false);
            given(userRepository.existsByEmail(EMAIL)).willReturn(false);
            given(roleRepository.findByName(ERole.ROLE_CLIENTE)).willReturn(Optional.of(roleCliente));
            given(passwordEncoder.encode(PASSWORD)).willReturn(ENCODED_PASSWORD);
            given(userRepository.save(any(User.class))).willReturn(savedUser);

            MessageResponse response = authService.register(request);

            assertThat(response.message()).isEqualTo("Usuario registrado exitosamente");

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            User captured = captor.getValue();
            assertThat(captured.getUsername()).isEqualTo(USERNAME);
            assertThat(captured.getEmail()).isEqualTo(EMAIL);
            assertThat(captured.getPassword()).isEqualTo(ENCODED_PASSWORD);
            assertThat(captured.getFirstName()).isEqualTo(FIRST_NAME);
            assertThat(captured.getLastName()).isEqualTo(LAST_NAME);
            assertThat(captured.isEnabled()).isTrue();
            assertThat(captured.getRoles()).hasSize(1);
            assertThat(captured.getRoles().iterator().next().getName()).isEqualTo(ERole.ROLE_CLIENTE);
        }

        @Test
        @DisplayName("should register user with default role when roles list is empty")
        void shouldRegisterWithDefaultRole_whenRolesEmpty() {
            var request = new RegisterRequest(USERNAME, EMAIL, PASSWORD, FIRST_NAME, LAST_NAME, Set.of());
            var roleCliente = Role.builder().id(3L).name(ERole.ROLE_CLIENTE).build();
            var savedUser = buildUserWithRoles(Set.of(roleCliente));

            given(userRepository.existsByUsername(USERNAME)).willReturn(false);
            given(userRepository.existsByEmail(EMAIL)).willReturn(false);
            given(roleRepository.findByName(ERole.ROLE_CLIENTE)).willReturn(Optional.of(roleCliente));
            given(passwordEncoder.encode(PASSWORD)).willReturn(ENCODED_PASSWORD);
            given(userRepository.save(any(User.class))).willReturn(savedUser);

            MessageResponse response = authService.register(request);

            assertThat(response.message()).isEqualTo("Usuario registrado exitosamente");
        }

        @Test
        @DisplayName("should register user with specified roles")
        void shouldRegisterWithCustomRoles() {
            var request = new RegisterRequest(USERNAME, EMAIL, PASSWORD, FIRST_NAME, LAST_NAME, Set.of("ADMIN", "EMPLEADO"));
            var roleAdmin = Role.builder().id(1L).name(ERole.ROLE_ADMIN).build();
            var roleEmpleado = Role.builder().id(2L).name(ERole.ROLE_EMPLEADO).build();
            var savedUser = buildUserWithRoles(Set.of(roleAdmin, roleEmpleado));

            given(userRepository.existsByUsername(USERNAME)).willReturn(false);
            given(userRepository.existsByEmail(EMAIL)).willReturn(false);
            given(roleRepository.findByName(ERole.ROLE_ADMIN)).willReturn(Optional.of(roleAdmin));
            given(roleRepository.findByName(ERole.ROLE_EMPLEADO)).willReturn(Optional.of(roleEmpleado));
            given(passwordEncoder.encode(PASSWORD)).willReturn(ENCODED_PASSWORD);
            given(userRepository.save(any(User.class))).willReturn(savedUser);

            MessageResponse response = authService.register(request);

            assertThat(response.message()).isEqualTo("Usuario registrado exitosamente");

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            assertThat(captor.getValue().getRoles()).hasSize(2);
        }

        @ParameterizedTest
        @CsvSource({
            "username, jperez",
            "email, jperez@example.com"
        })
        @DisplayName("should throw DuplicateResourceException when field already exists")
        void shouldThrowException_whenDuplicateFieldExists(String field, String expectedContent) {
            var request = new RegisterRequest(USERNAME, EMAIL, PASSWORD, FIRST_NAME, LAST_NAME, Set.of());

            if ("username".equals(field)) {
                given(userRepository.existsByUsername(USERNAME)).willReturn(true);
            } else {
                given(userRepository.existsByUsername(USERNAME)).willReturn(false);
                given(userRepository.existsByEmail(EMAIL)).willReturn(true);
            }

            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining(expectedContent);

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw BadRequestException when role name is invalid")
        void shouldThrowException_whenInvalidRole() {
            var request = new RegisterRequest(USERNAME, EMAIL, PASSWORD, FIRST_NAME, LAST_NAME, Set.of("INVALIDO"));

            given(userRepository.existsByUsername(USERNAME)).willReturn(false);
            given(userRepository.existsByEmail(EMAIL)).willReturn(false);

            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Rol inválido");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw BadRequestException when role not found in database")
        void shouldThrowException_whenRoleNotFound() {
            var request = new RegisterRequest(USERNAME, EMAIL, PASSWORD, FIRST_NAME, LAST_NAME, Set.of("ADMIN"));

            given(userRepository.existsByUsername(USERNAME)).willReturn(false);
            given(userRepository.existsByEmail(EMAIL)).willReturn(false);
            given(roleRepository.findByName(ERole.ROLE_ADMIN)).willReturn(Optional.empty());

            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Rol no encontrado");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw BadRequestException when default role not found")
        void shouldThrowException_whenDefaultRoleNotFound() {
            var request = new RegisterRequest(USERNAME, EMAIL, PASSWORD, FIRST_NAME, LAST_NAME, null);

            given(userRepository.existsByUsername(USERNAME)).willReturn(false);
            given(userRepository.existsByEmail(EMAIL)).willReturn(false);
            given(roleRepository.findByName(ERole.ROLE_CLIENTE)).willReturn(Optional.empty());

            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Rol no encontrado");

            verify(userRepository, never()).save(any());
        }
    }

    private User buildUser() {
        return User.builder()
                .id(USER_ID)
                .username(USERNAME)
                .email(EMAIL)
                .password(ENCODED_PASSWORD)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .enabled(true)
                .roles(Set.of(Role.builder().id(1L).name(ERole.ROLE_ADMIN).build()))
                .build();
    }

    private User buildUserWithRoles(Set<Role> roles) {
        return User.builder()
                .id(USER_ID)
                .username(USERNAME)
                .email(EMAIL)
                .password(ENCODED_PASSWORD)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .enabled(true)
                .roles(roles)
                .build();
    }

    private Authentication givenAuthentication(String username, String role) {
        Authentication auth = org.mockito.Mockito.mock(Authentication.class);
        given(auth.getName()).willReturn(username);
        lenient().when(auth.getAuthorities()).thenReturn((Collection) List.of(new SimpleGrantedAuthority(role)));
        return auth;
    }
}
