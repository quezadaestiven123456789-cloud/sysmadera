package com.madera.sys_madera.service.impl;

import com.madera.sys_madera.dto.request.ClientRequest;
import com.madera.sys_madera.dto.response.ClientResponse;
import com.madera.sys_madera.dto.response.PagedResponse;
import com.madera.sys_madera.exception.DuplicateResourceException;
import com.madera.sys_madera.exception.ResourceNotFoundException;
import com.madera.sys_madera.model.Client;
import com.madera.sys_madera.repository.ClientRepository;
import com.madera.sys_madera.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;

    @Override
    @Transactional
    public ClientResponse create(ClientRequest request) {
        if (request.email() != null && clientRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException(
                    "El email '" + request.email() + "' ya está registrado");
        }
        if (request.rfc() != null && clientRepository.existsByRfc(request.rfc())) {
            throw new DuplicateResourceException(
                    "El RFC '" + request.rfc() + "' ya está registrado");
        }

        Client client = Client.builder()
                .name(request.name())
                .email(request.email())
                .phone(request.phone())
                .address(request.address())
                .rfc(request.rfc())
                .build();

        client = clientRepository.save(client);
        return toResponse(client);
    }

    @Override
    @Transactional
    public ClientResponse update(Long id, ClientRequest request) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", id));

        if (request.email() != null && !request.email().equals(client.getEmail())
                && clientRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException(
                    "El email '" + request.email() + "' ya está registrado");
        }

        client.setName(request.name());
        client.setEmail(request.email());
        client.setPhone(request.phone());
        client.setAddress(request.address());
        if (request.rfc() != null) {
            client.setRfc(request.rfc());
        }

        client = clientRepository.save(client);
        return toResponse(client);
    }

    @Override
    public ClientResponse findById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", id));
        return toResponse(client);
    }

    @Override
    public PagedResponse<ClientResponse> findAll(int page, int size, String sort, String direction, String search) {
        Sort.Direction dir = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sort));

        Page<Client> clients;
        if (search != null && !search.isEmpty()) {
            clients = clientRepository.findByNameContainingIgnoreCase(search, pageable);
        } else {
            clients = clientRepository.findAll(pageable);
        }

        List<ClientResponse> content = clients.getContent().stream()
                .map(this::toResponse)
                .toList();

        return new PagedResponse<>(
                content,
                clients.getNumber(),
                clients.getSize(),
                clients.getTotalElements(),
                clients.getTotalPages(),
                clients.isLast()
        );
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", id));
        clientRepository.delete(client);
    }

    private ClientResponse toResponse(Client client) {
        return new ClientResponse(
                client.getId(),
                client.getName(),
                client.getEmail(),
                client.getPhone(),
                client.getAddress(),
                client.getRfc(),
                client.getUser() != null ? client.getUser().getId() : null,
                client.getCreatedAt(),
                client.getUpdatedAt()
        );
    }

}
