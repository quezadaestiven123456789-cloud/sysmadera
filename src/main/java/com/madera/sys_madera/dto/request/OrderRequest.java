package com.madera.sys_madera.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record OrderRequest(
        @NotNull(message = "El ID del cliente es obligatorio")
        Long clientId,
        String notes,
        @NotEmpty(message = "Debe incluir al menos un detalle")
        @Valid
        List<OrderDetailRequest> details
) {

    public record OrderDetailRequest(
            @NotNull(message = "El ID del mueble es obligatorio")
            Long furnitureId,
            @NotNull(message = "La cantidad es obligatoria")
            Integer quantity
    ) {}

}
