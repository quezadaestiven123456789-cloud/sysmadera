package com.madera.sys_madera.dto.request;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record InvoiceRequest(
        @NotNull(message = "El ID de la orden es obligatorio")
        Long orderId,
        @NotNull(message = "La fecha de emisión es obligatoria")
        LocalDate issueDate,
        @NotNull(message = "La fecha de vencimiento es obligatoria")
        LocalDate dueDate,
        BigDecimal paidAmount,
        String notes
) {}
