package com.madera.sys_madera.config;

import com.madera.sys_madera.model.ERole;
import com.madera.sys_madera.model.NumberSequence;
import com.madera.sys_madera.model.Role;
import com.madera.sys_madera.model.User;
import com.madera.sys_madera.repository.NumberSequenceRepository;
import com.madera.sys_madera.repository.RoleRepository;
import com.madera.sys_madera.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final NumberSequenceRepository numberSequenceRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Override
    public void run(String... args) {
        for (ERole roleName : ERole.values()) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                Role role = Role.builder()
                        .name(roleName)
                        .build();
                roleRepository.save(role);
                log.info("Role seeded: {}", roleName);
            }
        }

        seedSequence("ORDER_SEQ");
        seedSequence("INVOICE_SEQ");

        seedAdminUser();
    }

    private void seedSequence(String seqKey) {
        if (numberSequenceRepository.findById(seqKey).isEmpty()) {
            NumberSequence seq = new NumberSequence(seqKey, 1L);
            numberSequenceRepository.save(seq);
            log.info("Sequence seeded: {} = 1", seqKey);
        }
    }

    private void seedAdminUser() {
        if (adminUsername == null || adminPassword == null) {
            log.warn("Admin bootstrap credentials not configured, skipping");
            return;
        }

        if (userRepository.existsByUsername(adminUsername)) {
            log.info("Admin user '{}' already exists, skipping", adminUsername);
            return;
        }

        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                .orElseThrow(() -> new IllegalStateException(
                        "ROLE_ADMIN not found — roles must be seeded before admin user"));

        User admin = User.builder()
                .username(adminUsername)
                .password(passwordEncoder.encode(adminPassword))
                .email(adminEmail)
                .firstName("Admin")
                .lastName("Sistema")
                .enabled(true)
                .roles(Set.of(adminRole))
                .build();

        userRepository.save(admin);
        log.info("Admin user '{}' created with role ROLE_ADMIN", adminUsername);
    }

}
