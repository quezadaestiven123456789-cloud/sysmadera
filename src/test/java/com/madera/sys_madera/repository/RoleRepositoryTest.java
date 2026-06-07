package com.madera.sys_madera.repository;

import com.madera.sys_madera.config.JpaConfig;
import com.madera.sys_madera.model.ERole;
import com.madera.sys_madera.model.Role;
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
@DisplayName("RoleRepository")
class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    private Role roleAdmin;
    private Role roleEmpleado;
    private Role roleCliente;

    @BeforeEach
    void setUp() {
        roleRepository.deleteAll();

        roleAdmin = Role.builder()
                .name(ERole.ROLE_ADMIN)
                .build();

        roleEmpleado = Role.builder()
                .name(ERole.ROLE_EMPLEADO)
                .build();

        roleCliente = Role.builder()
                .name(ERole.ROLE_CLIENTE)
                .build();
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("should persist role with generated id")
        void shouldSaveRole() {
            Role saved = roleRepository.save(roleAdmin);

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getName()).isEqualTo(ERole.ROLE_ADMIN);
        }

        @Test
        @DisplayName("should persist all role types")
        void shouldSaveAllRoleTypes() {
            Role savedAdmin = roleRepository.save(roleAdmin);
            Role savedEmpleado = roleRepository.save(roleEmpleado);
            Role savedCliente = roleRepository.save(roleCliente);

            assertThat(savedAdmin.getName()).isEqualTo(ERole.ROLE_ADMIN);
            assertThat(savedEmpleado.getName()).isEqualTo(ERole.ROLE_EMPLEADO);
            assertThat(savedCliente.getName()).isEqualTo(ERole.ROLE_CLIENTE);
            assertThat(roleRepository.count()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("should return role when exists")
        void shouldFindById() {
            Role saved = roleRepository.save(roleAdmin);

            Optional<Role> found = roleRepository.findById(saved.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getName()).isEqualTo(ERole.ROLE_ADMIN);
        }

        @Test
        @DisplayName("should return empty when not found")
        void shouldReturnEmpty_whenNotFound() {
            Optional<Role> found = roleRepository.findById(999L);

            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByName")
    class FindByName {

        @Test
        @DisplayName("should return role when name exists")
        void shouldFindByName() {
            roleRepository.save(roleAdmin);

            Optional<Role> found = roleRepository.findByName(ERole.ROLE_ADMIN);

            assertThat(found).isPresent();
            assertThat(found.get().getName()).isEqualTo(ERole.ROLE_ADMIN);
        }

        @Test
        @DisplayName("should return role for ROLE_EMPLEADO")
        void shouldFindByName_empleado() {
            roleRepository.save(roleEmpleado);

            Optional<Role> found = roleRepository.findByName(ERole.ROLE_EMPLEADO);

            assertThat(found).isPresent();
            assertThat(found.get().getName()).isEqualTo(ERole.ROLE_EMPLEADO);
        }

        @Test
        @DisplayName("should return role for ROLE_CLIENTE")
        void shouldFindByName_cliente() {
            roleRepository.save(roleCliente);

            Optional<Role> found = roleRepository.findByName(ERole.ROLE_CLIENTE);

            assertThat(found).isPresent();
            assertThat(found.get().getName()).isEqualTo(ERole.ROLE_CLIENTE);
        }

        @Test
        @DisplayName("should return empty when name does not exist")
        void shouldReturnEmpty_whenNameNotFound() {
            roleRepository.save(roleAdmin);

            Optional<Role> found = roleRepository.findByName(ERole.ROLE_CLIENTE);

            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("should return all saved roles")
        void shouldFindAll() {
            roleRepository.save(roleAdmin);
            roleRepository.save(roleEmpleado);

            var roles = roleRepository.findAll();

            assertThat(roles).hasSize(2);
            assertThat(roles).extracting(Role::getName)
                    .containsExactlyInAnyOrder(ERole.ROLE_ADMIN, ERole.ROLE_EMPLEADO);
        }

        @Test
        @DisplayName("should return empty list when no roles exist")
        void shouldReturnEmpty_whenNoRoles() {
            var roles = roleRepository.findAll();

            assertThat(roles).isEmpty();
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("should remove role from database")
        void shouldDeleteRole() {
            Role saved = roleRepository.save(roleAdmin);

            roleRepository.deleteById(saved.getId());

            assertThat(roleRepository.findById(saved.getId())).isEmpty();
        }

        @Test
        @DisplayName("should not affect other roles when deleting one")
        void shouldNotAffectOtherRoles() {
            roleRepository.save(roleAdmin);
            Role savedEmpleado = roleRepository.save(roleEmpleado);

            roleRepository.deleteById(savedEmpleado.getId());

            assertThat(roleRepository.count()).isEqualTo(1);
            assertThat(roleRepository.findByName(ERole.ROLE_ADMIN)).isPresent();
            assertThat(roleRepository.findByName(ERole.ROLE_EMPLEADO)).isEmpty();
        }
    }

    @Nested
    @DisplayName("constraints")
    class Constraints {

        @Test
        @DisplayName("should enforce unique role name constraint")
        void shouldEnforceUniqueName() {
            roleRepository.saveAndFlush(roleAdmin);

            var duplicate = Role.builder()
                    .name(ERole.ROLE_ADMIN)
                    .build();

            assertThatThrownBy(() -> roleRepository.saveAndFlush(duplicate))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }
    }
}
