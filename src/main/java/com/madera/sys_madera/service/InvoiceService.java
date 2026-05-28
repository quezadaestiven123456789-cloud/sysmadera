package com.madera.sys_madera.service;

import com.madera.sys_madera.dto.request.InvoiceRequest;
import com.madera.sys_madera.dto.response.InvoiceResponse;
import com.madera.sys_madera.dto.response.PagedResponse;

public interface InvoiceService {

    InvoiceResponse create(InvoiceRequest request);

    InvoiceResponse findById(Long id);

    InvoiceResponse findByOrderId(Long orderId);

    PagedResponse<InvoiceResponse> findAll(int page, int size, String sort, String direction, String status);

    InvoiceResponse registerPayment(Long id, java.math.BigDecimal amount);

}
