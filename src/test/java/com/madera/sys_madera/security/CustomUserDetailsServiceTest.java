package com.madera.sys_madera.security;

import com.madera.sys_madera.model.ERole;
import com.madera.sys_madera.model.Role;
import com.madera.sys_madera.model.User;
import com.madera.sys_madera.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService")
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private static final String USERNAME = "juanperez";
    private static final String PASSWORD = "encodedPassword123";

    @Nested
    @DisplayName("loadUserByUsername")
    class LoadUserByUsername {

        @Test
        @DisplayName("should return UserDetails when user exists")
        void shouldReturnUserDetails_whenUserExists() {
            var user = User.builder()
                    .username(USERNAME)
                    .password(PASSWORD)
                    .enabled(true)
                    .roles(Set.of(Role.builder().name(ERole.ROLE_ADMIN).build()))
                    .build();

            given(userRepository.findByUsername(USERNAME)).willReturn(Optional.of(user));

            UserDetails userDetails = customUserDetailsService.loadUserByUsername(USERNAME);

            assertThat(userDetails).isNotNull();
            assertThat(userDetails.getUsername()).isEqualTo(USERNAME);
            assertThat(userDetails.getPassword()).isEqualTo(PASSWORD);
            assertThat(userDetails.isEnabled()).isTrue();
            assertThat(userDetails.isAccountNonExpired()).isTrue();
            assertThat(userDetails.isAccountNonLocked()).isTrue();
            assertThat(userDetails.isCredentialsNonExpired()).isTrue();
            assertThat(userDetails.getAuthorities())
                    .extracting("authority")
                    .containsExactly("ROLE_ADMIN");
        }

        @Test
        @DisplayName("should throw UsernameNotFoundException when user not found")
        void shouldThrowException_whenUserNotFound() {
            given(userRepository.findByUsername(USERNAME)).willReturn(Optional.empty());

            assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(USERNAME))
                    .isInstanceOf(UsernameNotFoundException.class)
                    .hasMessageContaining(USERNAME);
        }
    }

}
