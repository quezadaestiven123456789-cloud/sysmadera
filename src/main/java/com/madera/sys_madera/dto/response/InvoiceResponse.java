package com.madera.sys_madera.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record InvoiceResponse(
        Long id,
        String invoiceNumber,
        LocalDate issueDate,
        LocalDate dueDate,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        BigDecimal balance,
        String status,
        String notes,
        Long orderId,
        String orderNumber,
        String clientName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
