package com.ppaw.service;

import com.ppaw.dataaccess.entity.User;
import com.ppaw.dataaccess.repository.UserRepository;
import com.ppaw.service.dto.AuthRequest;
import com.ppaw.service.dto.AuthResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;

    @Transactional
    public AuthResponse register(AuthRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());
        
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = User.builder()
            .email(request.getEmail())
            .passwordHash(request.getPassword()) // Simple auth, no hashing
            .role(User.UserRole.USER)
            .build();

        user = userRepository.save(user);
        log.info("User registered successfully: {}", user.getId());
        
        return new AuthResponse(user.getId(), user.getEmail());
    }

    public AuthResponse login(AuthRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());
        
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!user.getPasswordHash().equals(request.getPassword())) {
            log.warn("Failed login attempt for email: {}", request.getEmail());
            throw new IllegalArgumentException("Invalid credentials");
        }

        log.info("User logged in successfully: {}", user.getId());
        return new AuthResponse(user.getId(), user.getEmail());
    }

    public User getUserById(UUID userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
