package com.madera.sys_madera.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "El username es obligatorio")
        String username,
        @NotBlank(message = "La contraseña es obligatoria")
        String password
) {}
