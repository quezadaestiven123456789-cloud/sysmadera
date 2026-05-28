package com.madera.sys_madera.service;

import com.madera.sys_madera.dto.request.FurnitureRequest;
import com.madera.sys_madera.dto.response.FurnitureResponse;
import com.madera.sys_madera.dto.response.PagedResponse;

public interface FurnitureService {

    FurnitureResponse create(FurnitureRequest request);

    FurnitureResponse update(Long id, FurnitureRequest request);

    FurnitureResponse findById(Long id);

    PagedResponse<FurnitureResponse> findAll(int page, int size, String sort, String direction, String search, String category);

    void delete(Long id);

}
