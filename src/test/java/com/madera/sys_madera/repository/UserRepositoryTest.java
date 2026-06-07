package com.madera.sys_madera.repository;

import com.madera.sys_madera.config.JpaConfig;
import com.madera.sys_madera.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(JpaConfig.class)
@DisplayName("UserRepository")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        user = User.builder()
                .username("jperez")
                .email("jperez@example.com")
                .password("$2a$10$encryptedpassword")
                .firstName("Juan")
                .lastName("Pérez")
                .enabled(true)
                .build();
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("should persist user with generated id and timestamps")
        void shouldSaveUser() {
            User saved = userRepository.save(user);

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getUsername()).isEqualTo("jperez");
            assertThat(saved.getEmail()).isEqualTo("jperez@example.com");
            assertThat(saved.getFirstName()).isEqualTo("Juan");
            assertThat(saved.getLastName()).isEqualTo("Pérez");
            assertThat(saved.isEnabled()).isTrue();
            assertThat(saved.getCreatedAt()).isNotNull();
            assertThat(saved.getUpdatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("should return user when exists")
        void shouldFindById() {
            User saved = userRepository.save(user);

            Optional<User> found = userRepository.findById(saved.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getUsername()).isEqualTo("jperez");
        }

        @Test
        @DisplayName("should return empty when not found")
        void shouldReturnEmpty_whenNotFound() {
            Optional<User> found = userRepository.findById(999L);

            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByUsername")
    class FindByUsername {

        @Test
        @DisplayName("should return user when username exists")
        void shouldFindByUsername() {
            userRepository.save(user);

            Optional<User> found = userRepository.findByUsername("jperez");

            assertThat(found).isPresent();
            assertThat(found.get().getEmail()).isEqualTo("jperez@example.com");
        }

        @Test
        @DisplayName("should return empty when username does not exist")
        void shouldReturnEmpty_whenUsernameNotFound() {
            Optional<User> found = userRepository.findByUsername("noexiste");

            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByEmail")
    class FindByEmail {

        @Test
        @DisplayName("should return user when email exists")
        void shouldFindByEmail() {
            userRepository.save(user);

            Optional<User> found = userRepository.findByEmail("jperez@example.com");

            assertThat(found).isPresent();
            assertThat(found.get().getUsername()).isEqualTo("jperez");
        }

        @Test
        @DisplayName("should return empty when email does not exist")
        void shouldReturnEmpty_whenEmailNotFound() {
            Optional<User> found = userRepository.findByEmail("no@exists.com");

            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsByUsername")
    class ExistsByUsername {

        @Test
        @DisplayName("should return true when username exists")
        void shouldReturnTrue_whenUsernameExists() {
            userRepository.save(user);

            boolean exists = userRepository.existsByUsername("jperez");

            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("should return false when username does not exist")
        void shouldReturnFalse_whenUsernameNotExists() {
            boolean exists = userRepository.existsByUsername("noexiste");

            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("existsByEmail")
    class ExistsByEmail {

        @Test
        @DisplayName("should return true when email exists")
        void shouldReturnTrue_whenEmailExists() {
            userRepository.save(user);

            boolean exists = userRepository.existsByEmail("jperez@example.com");

            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("should return false when email does not exist")
        void shouldReturnFalse_whenEmailNotExists() {
            boolean exists = userRepository.existsByEmail("no@exists.com");

            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("should remove user from database")
        void shouldDeleteUser() {
            User saved = userRepository.save(user);

            userRepository.deleteById(saved.getId());

            assertThat(userRepository.findById(saved.getId())).isEmpty();
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("should reflect changes after save with same id")
        void shouldUpdateUser() {
            User saved = userRepository.save(user);

            saved.setFirstName("Juan Manuel");
            saved.setEnabled(false);
            userRepository.save(saved);

            User updated = userRepository.findById(saved.getId()).orElseThrow();
            assertThat(updated.getFirstName()).isEqualTo("Juan Manuel");
            assertThat(updated.isEnabled()).isFalse();
        }
    }

    @Nested
    @DisplayName("constraints")
    class Constraints {

        @Test
        @DisplayName("should enforce unique username constraint")
        void shouldEnforceUniqueUsername() {
            userRepository.saveAndFlush(user);

            var duplicate = User.builder()
                    .username("jperez")
                    .email("otro@example.com")
                    .password("pass")
                    .firstName("Otro")
                    .lastName("Usuario")
                    .build();

            assertThatThrownBy(() -> userRepository.saveAndFlush(duplicate))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("should enforce unique email constraint")
        void shouldEnforceUniqueEmail() {
            userRepository.saveAndFlush(user);

            var duplicate = User.builder()
                    .username("otrousuario")
                    .email("jperez@example.com")
                    .password("pass")
                    .firstName("Otro")
                    .lastName("Usuario")
                    .build();

            assertThatThrownBy(() -> userRepository.saveAndFlush(duplicate))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }
    }
}
