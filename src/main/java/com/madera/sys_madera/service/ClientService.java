package com.madera.sys_madera.service;

import com.madera.sys_madera.dto.request.ClientRequest;
import com.madera.sys_madera.dto.response.ClientResponse;
import com.madera.sys_madera.dto.response.PagedResponse;

import java.util.List;

public interface ClientService {

    ClientResponse create(ClientRequest request);

    ClientResponse update(Long id, ClientRequest request);

    ClientResponse findById(Long id);

    PagedResponse<ClientResponse> findAll(int page, int size, String sort, String direction, String search);

    void delete(Long id);

}
