package com.pruebatecnica.bancocuscatlan.integration;

import com.pruebatecnica.bancocuscatlan.TestcontainersConfiguration;
import com.pruebatecnica.bancocuscatlan.domain.entity.User;
import com.pruebatecnica.bancocuscatlan.domain.enums.Role;
import com.pruebatecnica.bancocuscatlan.repository.ReservationRepository;
import com.pruebatecnica.bancocuscatlan.repository.UserRepository;
import com.pruebatecnica.bancocuscatlan.security.JwtTokenProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Cubre el hueco de testing identificado en la auditoría: ningún test
 * verificaba la autorización real por rol a nivel HTTP (403 para USER en
 * rutas de ADMIN, 200 en las propias).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "spring.profiles.active=test")
@Import(TestcontainersConfiguration.class)
class AuthorizationSecurityIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ReservationRepository reservationRepository;

    private String userToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        User user = userRepository.save(User.builder()
                .name("User Authz")
                .email("user-authz@example.com")
                .password(passwordEncoder.encode("pwd12345"))
                .role(Role.USER)
                .build());
        User admin = userRepository.save(User.builder()
                .name("Admin Authz")
                .email("admin-authz@example.com")
                .password(passwordEncoder.encode("pwd12345"))
                .role(Role.ADMIN)
                .build());
        userToken = jwtTokenProvider.generateToken(user);
        adminToken = jwtTokenProvider.generateToken(admin);
    }

    @AfterEach
    void tearDown() {
        reservationRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void userIsForbiddenFromListingAllReservations() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/reservations", HttpMethod.GET, authHeader(userToken), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void userIsForbiddenFromAdminEndpoints() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/admin/reservations", HttpMethod.GET, authHeader(userToken), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void adminCanListAllReservations() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/reservations", HttpMethod.GET, authHeader(adminToken), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void userCanAccessOwnReservations() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/reservations/my", HttpMethod.GET, authHeader(userToken), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private HttpEntity<Void> authHeader(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return new HttpEntity<>(headers);
    }
}
