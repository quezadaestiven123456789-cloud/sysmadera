package com.madera.sys_madera.service;

import com.madera.sys_madera.dto.request.WoodInventoryRequest;
import com.madera.sys_madera.dto.response.WoodInventoryResponse;
import com.madera.sys_madera.dto.response.PagedResponse;

import java.util.List;

public interface WoodInventoryService {

    WoodInventoryResponse create(WoodInventoryRequest request);

    WoodInventoryResponse update(Long id, WoodInventoryRequest request);

    WoodInventoryResponse findById(Long id);

    PagedResponse<WoodInventoryResponse> findAll(int page, int size, String sort, String direction, String search);

    void delete(Long id);

    List<WoodInventoryResponse> findLowStock();

}
