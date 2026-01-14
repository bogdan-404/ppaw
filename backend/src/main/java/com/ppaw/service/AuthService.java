package com.ppaw.service;

import com.ppaw.dataaccess.entity.Plan;
import com.ppaw.dataaccess.entity.Subscription;
import com.ppaw.dataaccess.entity.Subscription.SubscriptionStatus;
import com.ppaw.dataaccess.entity.User;
import com.ppaw.dataaccess.repository.SubscriptionRepository;
import com.ppaw.dataaccess.repository.UserRepository;
import com.ppaw.service.dto.AuthRequest;
import com.ppaw.service.dto.AuthResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PlanService planService;

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
        
        // Automatically assign FREE plan subscription
        createFreeSubscription(user);
        
        return new AuthResponse(user.getId(), user.getEmail());
    }

    public AuthResponse login(AuthRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());
        
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        // Check if user is soft-deleted
        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            log.warn("Login attempt for soft-deleted user: {}", request.getEmail());
            throw new IllegalArgumentException("Invalid credentials");
        }

        if (!user.getPasswordHash().equals(request.getPassword())) {
            log.warn("Failed login attempt for email: {}", request.getEmail());
            throw new IllegalArgumentException("Invalid credentials");
        }

        log.info("User logged in successfully: {}", user.getId());
        return new AuthResponse(user.getId(), user.getEmail());
    }

    public User getUserById(UUID userId) {
        return userRepository.findByIdAndIsDeletedFalse(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private void createFreeSubscription(User user) {
        log.info("Creating free subscription for new user: {}", user.getId());
        Plan freePlan = planService.getPlanEntityByCode("FREE");
        
        Subscription subscription = Subscription.builder()
            .user(user)
            .plan(freePlan)
            .status(SubscriptionStatus.ACTIVE)
            .startAt(LocalDateTime.now())
            .build();
        
        subscriptionRepository.save(subscription);
        log.info("Free subscription created for user: {}", user.getId());
    }
}
