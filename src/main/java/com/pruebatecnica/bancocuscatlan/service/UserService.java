package com.pruebatecnica.bancocuscatlan.service;

import com.pruebatecnica.bancocuscatlan.domain.entity.User;
import com.pruebatecnica.bancocuscatlan.domain.enums.Role;
import com.pruebatecnica.bancocuscatlan.dto.CreateUserRequest;
import com.pruebatecnica.bancocuscatlan.dto.UserResponse;
import com.pruebatecnica.bancocuscatlan.exception.BadRequestException;
import com.pruebatecnica.bancocuscatlan.exception.EmailAlreadyExistsException;
import com.pruebatecnica.bancocuscatlan.exception.ResourceNotFoundException;
import com.pruebatecnica.bancocuscatlan.mapper.UserMapper;
import com.pruebatecnica.bancocuscatlan.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Ya existe un usuario con ese email");
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() != null ? request.getRole() : Role.USER);

        User created = userRepository.save(user);
        return userMapper.toResponse(created);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));
        return userMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .toList();
    }
}
