package com.madera.sys_madera.service;

import com.madera.sys_madera.dto.request.OrderRequest;
import com.madera.sys_madera.dto.response.OrderResponse;
import com.madera.sys_madera.dto.response.PagedResponse;

public interface OrderService {

    OrderResponse create(OrderRequest request);

    OrderResponse findById(Long id);

    PagedResponse<OrderResponse> findAll(int page, int size, String sort, String direction, String status);

    PagedResponse<OrderResponse> findByClientId(Long clientId, int page, int size, String sort, String direction);

    OrderResponse updateStatus(Long id, String status);

}
