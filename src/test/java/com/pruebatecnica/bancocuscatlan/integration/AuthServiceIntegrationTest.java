package com.pruebaTecnica.BancoCuscatlan.integration;

import com.pruebaTecnica.BancoCuscatlan.TestcontainersConfiguration;
import com.pruebaTecnica.BancoCuscatlan.domain.enums.Role;
import com.pruebaTecnica.BancoCuscatlan.dto.AuthResponse;
import com.pruebaTecnica.BancoCuscatlan.dto.LoginRequest;
import com.pruebaTecnica.BancoCuscatlan.dto.RegisterRequest;
import com.pruebaTecnica.BancoCuscatlan.dto.UserResponse;
import com.pruebaTecnica.BancoCuscatlan.repository.UserRepository;
import com.pruebaTecnica.BancoCuscatlan.service.AuthService;
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