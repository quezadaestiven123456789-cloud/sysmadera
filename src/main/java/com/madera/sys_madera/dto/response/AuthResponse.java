package com.madera.sys_madera.dto.response;

import java.util.List;

public record AuthResponse(
        String token,
        String type,
        Long id,
        String username,
        String email,
        List<String> roles
) {

    public AuthResponse {
        type = "Bearer";
    }

}
