package com.example.hospitalmanager.config;

import com.example.hospitalmanager.model.AppUser;
import com.example.hospitalmanager.model.Role;
import com.example.hospitalmanager.repository.UserRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataLoader {

    @Bean
    public ApplicationRunner seedAdministrator(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AdminSeedProperties properties) {
        return args -> {
            if (!properties.enabled()) {
                return;
            }
            if (properties.username() == null || properties.username().isBlank()
                    || properties.password() == null || properties.password().isBlank()) {
                throw new IllegalStateException(
                        "Set HOSPITAL_ADMIN_USERNAME and HOSPITAL_ADMIN_PASSWORD before enabling admin seeding.");
            }
            if (userRepository.existsByUsername(properties.username())) {
                AppUser existing = userRepository.findByUsername(properties.username()).orElseThrow();
                if (existing.getRole() != Role.ADMIN) {
                    throw new IllegalStateException("The configured seed username already belongs to a non-admin user.");
                }
                return;
            }

            AppUser administrator = new AppUser();
            administrator.setUsername(properties.username());
            administrator.setPassword(passwordEncoder.encode(properties.password()));
            administrator.setName(properties.name());
            administrator.setRole(Role.ADMIN);
            userRepository.save(administrator);
        };
    }
}
