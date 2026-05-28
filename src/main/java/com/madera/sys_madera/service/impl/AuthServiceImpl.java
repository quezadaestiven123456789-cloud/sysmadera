package com.madera.sys_madera.service.impl;

import com.madera.sys_madera.dto.request.LoginRequest;
import com.madera.sys_madera.dto.request.RegisterRequest;
import com.madera.sys_madera.dto.response.AuthResponse;
import com.madera.sys_madera.dto.response.MessageResponse;
import com.madera.sys_madera.exception.BadRequestException;
import com.madera.sys_madera.exception.DuplicateResourceException;
import com.madera.sys_madera.model.ERole;
import com.madera.sys_madera.model.Role;
import com.madera.sys_madera.model.User;
import com.madera.sys_madera.repository.RoleRepository;
import com.madera.sys_madera.repository.UserRepository;
import com.madera.sys_madera.security.jwt.JwtTokenProvider;
import com.madera.sys_madera.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        String token = jwtTokenProvider.generateToken(authentication.getName());

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow();

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return new AuthResponse(
                token,
                "Bearer",
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                roles
        );
    }

    @Override
    @Transactional
    public MessageResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException(
                    "El username '" + request.username() + "' ya está en uso");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException(
                    "El email '" + request.email() + "' ya está registrado");
        }

        Set<Role> roles = new HashSet<>();

        if (request.roles() == null || request.roles().isEmpty()) {
            Role defaultRole = roleRepository.findByName(ERole.ROLE_CLIENTE)
                    .orElseThrow(() -> new BadRequestException("Rol no encontrado"));
            roles.add(defaultRole);
        } else {
            request.roles().forEach(roleName -> {
                ERole eRole;
                try {
                    eRole = ERole.valueOf("ROLE_" + roleName.toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new BadRequestException("Rol inválido: " + roleName);
                }
                Role role = roleRepository.findByName(eRole)
                        .orElseThrow(() -> new BadRequestException("Rol no encontrado: " + roleName));
                roles.add(role);
            });
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .roles(roles)
                .build();

        userRepository.save(user);

        return new MessageResponse("Usuario registrado exitosamente");
    }

}
