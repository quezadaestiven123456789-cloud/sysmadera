package com.madera.sys_madera.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WoodInventoryResponse(
        Long id,
        String woodType,
        BigDecimal quantity,
        String unit,
        BigDecimal unitPrice,
        BigDecimal totalValue,
        String supplier,
        String description,
        BigDecimal minimumStock,
        boolean lowStock,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
