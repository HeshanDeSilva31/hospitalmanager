
package com.example.hospitalmanager.config;

import com.example.hospitalmanager.model.AppUser;
import com.example.hospitalmanager.model.Role;
import com.example.hospitalmanager.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminSeeder implements CommandLineRunner {

    private final AdminSeedProperties properties;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminSeeder(
            AdminSeedProperties properties,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.properties = properties;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!properties.enabled()) {
            return;
        }

        if (properties.username() == null || properties.username().isBlank()
                || properties.password() == null || properties.password().isBlank()) {
            throw new IllegalStateException(
                    "Admin username and password must be configured.");
        }

        var existingUser = userRepository.findByUsername(properties.username());

        if (existingUser.isPresent()) {
            if (existingUser.get().getRole() != Role.ADMIN) {
                throw new IllegalStateException(
                        "The configured admin username already belongs to a non-admin user. Choose another username.");
            }
            return;
        }

        AppUser admin = new AppUser(
                null,
                properties.username(),
                passwordEncoder.encode(properties.password()),
                Role.ADMIN,
                properties.name()
        );

        userRepository.save(admin);

        System.out.println("ADMIN account created successfully!");
    }
}
