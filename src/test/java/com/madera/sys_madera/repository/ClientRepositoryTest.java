package com.madera.sys_madera.repository;

import com.madera.sys_madera.config.JpaConfig;
import com.madera.sys_madera.model.Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(JpaConfig.class)
@DisplayName("ClientRepository")
class ClientRepositoryTest {

    @Autowired
    private ClientRepository clientRepository;

    private Client client;

    @BeforeEach
    void setUp() {
        clientRepository.deleteAll();
        client = Client.builder()
                .name("Juan Pérez")
                .email("juan@example.com")
                .phone("555-1234")
                .address("Av. Siempre Viva 742")
                .rfc("JUPE800101")
                .build();
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("should persist client with generated id and timestamps")
        void shouldSaveClient() {
            Client saved = clientRepository.save(client);

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getName()).isEqualTo("Juan Pérez");
            assertThat(saved.getEmail()).isEqualTo("juan@example.com");
            assertThat(saved.getPhone()).isEqualTo("555-1234");
            assertThat(saved.getAddress()).isEqualTo("Av. Siempre Viva 742");
            assertThat(saved.getRfc()).isEqualTo("JUPE800101");
            assertThat(saved.getCreatedAt()).isNotNull();
            assertThat(saved.getUpdatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("should return client when exists")
        void shouldFindById() {
            Client saved = clientRepository.save(client);

            Optional<Client> found = clientRepository.findById(saved.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getEmail()).isEqualTo("juan@example.com");
        }

        @Test
        @DisplayName("should return empty when not found")
        void shouldReturnEmpty_whenNotFound() {
            Optional<Client> found = clientRepository.findById(999L);

            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByEmail")
    class FindByEmail {

        @Test
        @DisplayName("should return client when email exists")
        void shouldFindByEmail() {
            clientRepository.save(client);

            Optional<Client> found = clientRepository.findByEmail("juan@example.com");

            assertThat(found).isPresent();
            assertThat(found.get().getName()).isEqualTo("Juan Pérez");
        }

        @Test
        @DisplayName("should return empty when email does not exist")
        void shouldReturnEmpty_whenEmailNotFound() {
            Optional<Client> found = clientRepository.findByEmail("no@exists.com");

            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByRfc")
    class FindByRfc {

        @Test
        @DisplayName("should return client when RFC exists")
        void shouldFindByRfc() {
            clientRepository.save(client);

            Optional<Client> found = clientRepository.findByRfc("JUPE800101");

            assertThat(found).isPresent();
            assertThat(found.get().getName()).isEqualTo("Juan Pérez");
        }

        @Test
        @DisplayName("should return empty when RFC does not exist")
        void shouldReturnEmpty_whenRfcNotFound() {
            Optional<Client> found = clientRepository.findByRfc("XXXX000000");

            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsByEmail")
    class ExistsByEmail {

        @Test
        @DisplayName("should return true when email exists")
        void shouldReturnTrue_whenEmailExists() {
            clientRepository.save(client);

            boolean exists = clientRepository.existsByEmail("juan@example.com");

            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("should return false when email does not exist")
        void shouldReturnFalse_whenEmailNotExists() {
            boolean exists = clientRepository.existsByEmail("no@exists.com");

            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("existsByRfc")
    class ExistsByRfc {

        @Test
        @DisplayName("should return true when RFC exists")
        void shouldReturnTrue_whenRfcExists() {
            clientRepository.save(client);

            boolean exists = clientRepository.existsByRfc("JUPE800101");

            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("should return false when RFC does not exist")
        void shouldReturnFalse_whenRfcNotExists() {
            boolean exists = clientRepository.existsByRfc("XXXX000000");

            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("findByNameContainingIgnoreCase")
    class FindByNameContainingIgnoreCase {

        @Test
        @DisplayName("should return matching clients ignoring case")
        void shouldFindByNameContainingIgnoreCase() {
            clientRepository.save(client);
            var another = Client.builder()
                    .name("María López")
                    .email("maria@example.com")
                    .rfc("MALO900101")
                    .build();
            clientRepository.save(another);

            Page<Client> result = clientRepository
                    .findByNameContainingIgnoreCase("juan", PageRequest.of(0, 10));

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getEmail()).isEqualTo("juan@example.com");
        }

        @Test
        @DisplayName("should return empty page when no match")
        void shouldReturnEmpty_whenNoMatch() {
            clientRepository.save(client);

            Page<Client> result = clientRepository
                    .findByNameContainingIgnoreCase("xyz", PageRequest.of(0, 10));

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should match regardless of case")
        void shouldMatchCaseInsensitive() {
            clientRepository.save(client);

            Page<Client> result = clientRepository
                    .findByNameContainingIgnoreCase("JUAN", PageRequest.of(0, 10));

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("should remove client from database")
        void shouldDeleteClient() {
            Client saved = clientRepository.save(client);

            clientRepository.deleteById(saved.getId());

            assertThat(clientRepository.findById(saved.getId())).isEmpty();
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("should reflect changes after save with same id")
        void shouldUpdateClient() {
            Client saved = clientRepository.save(client);

            saved.setName("Juan Actualizado");
            saved.setPhone("555-9999");
            clientRepository.save(saved);

            Client updated = clientRepository.findById(saved.getId()).orElseThrow();
            assertThat(updated.getName()).isEqualTo("Juan Actualizado");
            assertThat(updated.getPhone()).isEqualTo("555-9999");
        }
    }

    @Nested
    @DisplayName("constraints")
    class Constraints {

        @Test
        @DisplayName("should enforce unique email constraint")
        void shouldEnforceUniqueEmail() {
            clientRepository.saveAndFlush(client);

            var duplicate = Client.builder()
                    .name("Otro")
                    .email("juan@example.com")
                    .build();

            assertThatThrownBy(() -> clientRepository.saveAndFlush(duplicate))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("should enforce unique RFC constraint")
        void shouldEnforceUniqueRfc() {
            clientRepository.saveAndFlush(client);

            var duplicate = Client.builder()
                    .name("Otro")
                    .email("otro@example.com")
                    .rfc("JUPE800101")
                    .build();

            assertThatThrownBy(() -> clientRepository.saveAndFlush(duplicate))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }
    }
}
