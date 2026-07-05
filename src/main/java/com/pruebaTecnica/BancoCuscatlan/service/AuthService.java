package com.pruebaTecnica.BancoCuscatlan.service;

import com.pruebaTecnica.BancoCuscatlan.domain.entity.User;
import com.pruebaTecnica.BancoCuscatlan.domain.enums.Role;
import com.pruebaTecnica.BancoCuscatlan.dto.AuthResponse;
import com.pruebaTecnica.BancoCuscatlan.dto.CreateUserRequest;
import com.pruebaTecnica.BancoCuscatlan.dto.LoginRequest;
import com.pruebaTecnica.BancoCuscatlan.dto.RegisterRequest;
import com.pruebaTecnica.BancoCuscatlan.dto.UserResponse;
import com.pruebaTecnica.BancoCuscatlan.exception.UnauthorizedException;
import com.pruebaTecnica.BancoCuscatlan.repository.UserRepository;
import com.pruebaTecnica.BancoCuscatlan.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(
            UserService userService,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider
    ) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        CreateUserRequest createUserRequest = new CreateUserRequest(
                request.getName(),
                request.getEmail(),
                request.getPassword(),
                Role.USER
        );
        return userService.createUser(createUserRequest);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Credenciales inválidas"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Credenciales inválidas");
        }

        String token = jwtTokenProvider.generateToken(user);

        return AuthResponse.builder()
                .tokenType("Bearer")
                .accessToken(token)
                .expiresIn(jwtTokenProvider.expiresInSeconds())
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .build();
    }
}
