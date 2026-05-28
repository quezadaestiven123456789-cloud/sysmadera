package com.madera.sys_madera.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record FurnitureRequest(
        @NotBlank(message = "El nombre del mueble es obligatorio")
        String name,
        String description,
        @Positive(message = "El precio debe ser positivo")
        BigDecimal price,
        String woodType,
        String dimensions,
        String category,
        Integer stockQuantity,
        String imageUrl
) {}
