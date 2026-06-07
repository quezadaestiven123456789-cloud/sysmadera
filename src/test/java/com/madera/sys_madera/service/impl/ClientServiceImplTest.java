package com.madera.sys_madera.service.impl;

import com.madera.sys_madera.dto.request.ClientRequest;
import com.madera.sys_madera.dto.response.ClientResponse;
import com.madera.sys_madera.exception.DuplicateResourceException;
import com.madera.sys_madera.exception.ResourceNotFoundException;
import com.madera.sys_madera.model.Client;
import com.madera.sys_madera.repository.ClientRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClientServiceImpl")
class ClientServiceImplTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientServiceImpl clientService;

    private static final Long CLIENT_ID = 1L;
    private static final String CLIENT_NAME = "Juan Pérez";
    private static final String CLIENT_EMAIL = "juan@example.com";
    private static final String CLIENT_PHONE = "555-1234";
    private static final String CLIENT_ADDRESS = "Av. Siempre Viva 742";
    private static final String CLIENT_RFC = "JUPE800101";

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("should save and return client when request is valid")
        void shouldSaveClient_whenValidRequest() {
            var request = buildRequest();
            var savedClient = buildClient();

            given(clientRepository.existsByEmail(CLIENT_EMAIL)).willReturn(false);
            given(clientRepository.existsByRfc(CLIENT_RFC)).willReturn(false);
            given(clientRepository.save(any(Client.class))).willReturn(savedClient);

            ClientResponse response = clientService.create(request);

            assertThat(response.id()).isEqualTo(CLIENT_ID);
            assertThat(response.name()).isEqualTo(CLIENT_NAME);
            assertThat(response.email()).isEqualTo(CLIENT_EMAIL);
            assertThat(response.phone()).isEqualTo(CLIENT_PHONE);
            assertThat(response.address()).isEqualTo(CLIENT_ADDRESS);
            assertThat(response.rfc()).isEqualTo(CLIENT_RFC);

            ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
            verify(clientRepository).save(captor.capture());
            assertThat(captor.getValue().getName()).isEqualTo(CLIENT_NAME);
            assertThat(captor.getValue().getEmail()).isEqualTo(CLIENT_EMAIL);
        }

        @Test
        @DisplayName("should throw DuplicateResourceException when email already exists")
        void shouldThrowException_whenEmailAlreadyExists() {
            var request = buildRequest();

            given(clientRepository.existsByEmail(CLIENT_EMAIL)).willReturn(true);

            assertThatThrownBy(() -> clientService.create(request))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining(CLIENT_EMAIL);

            verify(clientRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw DuplicateResourceException when RFC already exists")
        void shouldThrowException_whenRfcAlreadyExists() {
            var request = buildRequest();

            given(clientRepository.existsByEmail(CLIENT_EMAIL)).willReturn(false);
            given(clientRepository.existsByRfc(CLIENT_RFC)).willReturn(true);

            assertThatThrownBy(() -> clientService.create(request))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining(CLIENT_RFC);

            verify(clientRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("should return client response when client exists")
        void shouldReturnClient_whenClientExists() {
            given(clientRepository.findById(CLIENT_ID)).willReturn(Optional.of(buildClient()));

            ClientResponse response = clientService.findById(CLIENT_ID);

            assertThat(response.id()).isEqualTo(CLIENT_ID);
            assertThat(response.name()).isEqualTo(CLIENT_NAME);
            assertThat(response.email()).isEqualTo(CLIENT_EMAIL);
            assertThat(response.phone()).isEqualTo(CLIENT_PHONE);
            assertThat(response.address()).isEqualTo(CLIENT_ADDRESS);
            assertThat(response.rfc()).isEqualTo(CLIENT_RFC);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when client not found")
        void shouldThrowException_whenClientNotFound() {
            given(clientRepository.findById(CLIENT_ID)).willReturn(Optional.empty());

            assertThatThrownBy(() -> clientService.findById(CLIENT_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Cliente")
                    .hasMessageContaining(String.valueOf(CLIENT_ID));
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("should update and return client when request is valid")
        void shouldUpdateClient_whenValidRequest() {
            var request = buildRequest();
            var existingClient = buildClient();
            existingClient.setEmail("old@email.com");

            given(clientRepository.findById(CLIENT_ID)).willReturn(Optional.of(existingClient));
            given(clientRepository.existsByEmail(CLIENT_EMAIL)).willReturn(false);
            given(clientRepository.save(any(Client.class))).willReturn(existingClient);

            ClientResponse response = clientService.update(CLIENT_ID, request);

            ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);

            verify(clientRepository).save(captor.capture());

            assertThat(captor.getValue().getEmail()).isEqualTo(CLIENT_EMAIL);

            assertThat(captor.getValue().getName()).isEqualTo(CLIENT_NAME);

            assertThat(response.id()).isEqualTo(CLIENT_ID);
            assertThat(response.name()).isEqualTo(CLIENT_NAME);
            assertThat(response.email()).isEqualTo(CLIENT_EMAIL);
            assertThat(captor.getValue().getPhone()).isEqualTo(CLIENT_PHONE);
            assertThat(captor.getValue().getAddress()).isEqualTo(CLIENT_ADDRESS);
            assertThat(captor.getValue().getRfc()).isEqualTo(CLIENT_RFC);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when client not found")
        void shouldThrowException_whenClientNotFound() {
            var request = buildRequest();

            given(clientRepository.findById(CLIENT_ID)).willReturn(Optional.empty());

            assertThatThrownBy(() -> clientService.update(CLIENT_ID, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Cliente");
        }

        @Test
        @DisplayName("should throw DuplicateResourceException when email already taken by another client")
        void shouldThrowException_whenEmailAlreadyTaken() {
            var request = buildRequest();
            var existingClient = buildClient();
            existingClient.setEmail("otro@email.com");

            given(clientRepository.findById(CLIENT_ID)).willReturn(Optional.of(existingClient));
            given(clientRepository.existsByEmail(CLIENT_EMAIL)).willReturn(true);

            assertThatThrownBy(() -> clientService.update(CLIENT_ID, request))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining(CLIENT_EMAIL);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("should delete client when client exists")
        void shouldDeleteClient_whenClientExists() {
            var existingClient = buildClient();

            given(clientRepository.findById(CLIENT_ID)).willReturn(Optional.of(existingClient));

            clientService.delete(CLIENT_ID);

            verify(clientRepository).delete(existingClient);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when client not found")
        void shouldThrowException_whenClientNotFound() {
            given(clientRepository.findById(CLIENT_ID)).willReturn(Optional.empty());

            assertThatThrownBy(() -> clientService.delete(CLIENT_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Cliente");
        }
    }

    private ClientRequest buildRequest() {
        return new ClientRequest(CLIENT_NAME, CLIENT_EMAIL, CLIENT_PHONE, CLIENT_ADDRESS, CLIENT_RFC);
    }

    private Client buildClient() {
        return Client.builder()
                .id(CLIENT_ID)
                .name(CLIENT_NAME)
                .email(CLIENT_EMAIL)
                .phone(CLIENT_PHONE)
                .address(CLIENT_ADDRESS)
                .rfc(CLIENT_RFC)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
