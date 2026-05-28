package com.madera.sys_madera.dto.response;

import java.time.LocalDateTime;

public record ClientResponse(
        Long id,
        String name,
        String email,
        String phone,
        String address,
        String rfc,
        Long userId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
