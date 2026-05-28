package com.madera.sys_madera.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FurnitureResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        String woodType,
        String dimensions,
        String category,
        Integer stockQuantity,
        Boolean active,
        String imageUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
