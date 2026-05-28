package com.madera.sys_madera.service;

import com.madera.sys_madera.dto.request.LoginRequest;
import com.madera.sys_madera.dto.request.RegisterRequest;
import com.madera.sys_madera.dto.response.AuthResponse;
import com.madera.sys_madera.dto.response.MessageResponse;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    MessageResponse register(RegisterRequest request);

}
