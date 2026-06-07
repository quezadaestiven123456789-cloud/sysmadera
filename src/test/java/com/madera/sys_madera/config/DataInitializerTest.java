package com.madera.sys_madera.config;

import com.madera.sys_madera.model.ERole;
import com.madera.sys_madera.model.Role;
import com.madera.sys_madera.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("DataInitializer")
class DataInitializerTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private DataInitializer dataInitializer;

    @Captor
    private ArgumentCaptor<Role> roleCaptor;

    private Role adminRole;
    private Role empleadoRole;
    private Role clienteRole;

    @BeforeEach
    void setUp() {
        adminRole = Role.builder().id(1L).name(ERole.ROLE_ADMIN).build();
        empleadoRole = Role.builder().id(2L).name(ERole.ROLE_EMPLEADO).build();
        clienteRole = Role.builder().id(3L).name(ERole.ROLE_CLIENTE).build();
    }

    @Nested
    @DisplayName("first execution — no roles exist")
    class FirstExecution {

        @Test
        @DisplayName("should create ROLE_ADMIN, ROLE_EMPLEADO, ROLE_CLIENTE")
        void shouldCreateAllRoles() throws Exception {
            given(roleRepository.findByName(any(ERole.class)))
                    .willReturn(Optional.empty());

            dataInitializer.run();

            then(roleRepository).should(times(3)).save(roleCaptor.capture());
            var savedRoles = roleCaptor.getAllValues();
            assertThat(savedRoles)
                    .extracting(Role::getName)
                    .containsExactly(ERole.ROLE_ADMIN, ERole.ROLE_EMPLEADO, ERole.ROLE_CLIENTE);
        }

        @Test
        @DisplayName("should call findByName for each ERole value")
        void shouldCallFindByNameForEachRole() throws Exception {
            given(roleRepository.findByName(any(ERole.class)))
                    .willReturn(Optional.empty());

            dataInitializer.run();

            then(roleRepository).should(times(3)).findByName(any(ERole.class));
            then(roleRepository).should(times(1)).findByName(ERole.ROLE_ADMIN);
            then(roleRepository).should(times(1)).findByName(ERole.ROLE_EMPLEADO);
            then(roleRepository).should(times(1)).findByName(ERole.ROLE_CLIENTE);
        }

        @Test
        @DisplayName("should pass ERole to Role.builder().name()")
        void shouldBuildRoleWithCorrectName() throws Exception {
            given(roleRepository.findByName(any(ERole.class)))
                    .willReturn(Optional.empty());

            dataInitializer.run();

            then(roleRepository).should(times(3)).save(roleCaptor.capture());
            assertThat(roleCaptor.getAllValues().get(0).getName()).isEqualTo(ERole.ROLE_ADMIN);
            assertThat(roleCaptor.getAllValues().get(1).getName()).isEqualTo(ERole.ROLE_EMPLEADO);
            assertThat(roleCaptor.getAllValues().get(2).getName()).isEqualTo(ERole.ROLE_CLIENTE);
        }
    }

    @Nested
    @DisplayName("idempotency — some or all roles already exist")
    class Idempotency {

        @Test
        @DisplayName("should not create roles when all already exist")
        void shouldNotCreateAny_whenAllExist() throws Exception {
            given(roleRepository.findByName(ERole.ROLE_ADMIN)).willReturn(Optional.of(adminRole));
            given(roleRepository.findByName(ERole.ROLE_EMPLEADO)).willReturn(Optional.of(empleadoRole));
            given(roleRepository.findByName(ERole.ROLE_CLIENTE)).willReturn(Optional.of(clienteRole));

            dataInitializer.run();

            then(roleRepository).should(never()).save(any(Role.class));
        }

        @Test
        @DisplayName("should create only missing roles")
        void shouldCreateOnlyMissing_whenSomeExist() throws Exception {
            given(roleRepository.findByName(ERole.ROLE_ADMIN)).willReturn(Optional.of(adminRole));
            given(roleRepository.findByName(ERole.ROLE_EMPLEADO)).willReturn(Optional.empty());
            given(roleRepository.findByName(ERole.ROLE_CLIENTE)).willReturn(Optional.of(clienteRole));

            dataInitializer.run();

            then(roleRepository).should(times(1)).save(roleCaptor.capture());
            assertThat(roleCaptor.getValue().getName()).isEqualTo(ERole.ROLE_EMPLEADO);
        }

        @Test
        @DisplayName("second run should not save anything if roles exist")
        void shouldNotSaveOnSecondRun_whenAlreadySeeded() throws Exception {
            given(roleRepository.findByName(any(ERole.class))).willReturn(Optional.empty());
            dataInitializer.run();
            then(roleRepository).should(times(3)).save(any(Role.class));

            given(roleRepository.findByName(ERole.ROLE_ADMIN)).willReturn(Optional.of(adminRole));
            given(roleRepository.findByName(ERole.ROLE_EMPLEADO)).willReturn(Optional.of(empleadoRole));
            given(roleRepository.findByName(ERole.ROLE_CLIENTE)).willReturn(Optional.of(clienteRole));
            org.mockito.Mockito.clearInvocations(roleRepository);

            dataInitializer.run();

            then(roleRepository).should(times(3)).findByName(any(ERole.class));
            then(roleRepository).should(never()).save(any(Role.class));
        }
    }

    @Nested
    @DisplayName("repository interaction")
    class RepositoryInteraction {

        @Test
        @DisplayName("should call findByName before each save")
        void shouldCheckExistenceBeforeSaving() throws Exception {
            given(roleRepository.findByName(any(ERole.class)))
                    .willReturn(Optional.empty());

            dataInitializer.run();

            var order = inOrder(roleRepository);
            order.verify(roleRepository).findByName(ERole.ROLE_ADMIN);
            order.verify(roleRepository).save(roleCaptor.capture());
            assertThat(roleCaptor.getValue().getName()).isEqualTo(ERole.ROLE_ADMIN);

            order.verify(roleRepository).findByName(ERole.ROLE_EMPLEADO);
            order.verify(roleRepository).save(roleCaptor.capture());
            assertThat(roleCaptor.getValue().getName()).isEqualTo(ERole.ROLE_EMPLEADO);

            order.verify(roleRepository).findByName(ERole.ROLE_CLIENTE);
            order.verify(roleRepository).save(roleCaptor.capture());
            assertThat(roleCaptor.getValue().getName()).isEqualTo(ERole.ROLE_CLIENTE);
        }
    }
}
