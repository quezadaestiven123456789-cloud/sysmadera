package com.madera.sys_madera.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record WoodInventoryRequest(
        @NotBlank(message = "El tipo de madera es obligatorio")
        String woodType,
        @Positive(message = "La cantidad debe ser positiva")
        BigDecimal quantity,
        @NotBlank(message = "La unidad es obligatoria")
        String unit,
        @Positive(message = "El precio unitario debe ser positivo")
        BigDecimal unitPrice,
        String supplier,
        String description,
        BigDecimal minimumStock
) {}
