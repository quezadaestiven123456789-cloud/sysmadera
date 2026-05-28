package com.madera.sys_madera.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ClientRequest(
        @NotBlank(message = "El nombre del cliente es obligatorio")
        String name,
        @Email(message = "El email debe ser válido")
        String email,
        String phone,
        String address,
        String rfc
) {}
