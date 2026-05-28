package com.madera.sys_madera.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        String orderNumber,
        String status,
        BigDecimal totalAmount,
        String notes,
        String clientName,
        Long clientId,
        List<OrderDetailResponse> details,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public record OrderDetailResponse(
            Long id,
            Long furnitureId,
            String furnitureName,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal
    ) {}

}
