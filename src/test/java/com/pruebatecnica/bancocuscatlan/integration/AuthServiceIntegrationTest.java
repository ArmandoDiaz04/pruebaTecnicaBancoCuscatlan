package com.pruebatecnica.bancocuscatlan.integration;

import com.pruebatecnica.bancocuscatlan.TestcontainersConfiguration;
import com.pruebatecnica.bancocuscatlan.domain.enums.Role;
import com.pruebatecnica.bancocuscatlan.dto.AuthResponse;
import com.pruebatecnica.bancocuscatlan.dto.LoginRequest;
import com.pruebatecnica.bancocuscatlan.dto.RegisterRequest;
import com.pruebatecnica.bancocuscatlan.dto.UserResponse;
import com.pruebatecnica.bancocuscatlan.repository.UserRepository;
import com.pruebatecnica.bancocuscatlan.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.profiles.active=test")
@Import(TestcontainersConfiguration.class)
class AuthServiceIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void registerAndLoginUsesRealPersistenceAndJwtGeneration() {
        RegisterRequest registerRequest = new RegisterRequest(
                "Ana Lopez",
                "ana.lopez@example.com",
                "supersecret"
        );

        UserResponse registered = authService.register(registerRequest);

        assertThat(registered.getId()).isNotNull();
        assertThat(registered.getEmail()).isEqualTo("ana.lopez@example.com");
        assertThat(registered.getRole()).isEqualTo(Role.USER);
        assertThat(userRepository.findByEmail("ana.lopez@example.com")).isPresent();
        assertThat(userRepository.findByEmail("ana.lopez@example.com").orElseThrow().getPassword())
                .isNotEqualTo("supersecret");

        AuthResponse authResponse = authService.login(new LoginRequest("ana.lopez@example.com", "supersecret"));

        assertThat(authResponse.getTokenType()).isEqualTo("Bearer");
        assertThat(authResponse.getAccessToken()).isNotBlank();
        assertThat(authResponse.getUserId()).isEqualTo(registered.getId());
        assertThat(authResponse.getRole()).isEqualTo(Role.USER);
    }
}