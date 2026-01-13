package com.ppaw.service;

import com.ppaw.dataaccess.entity.User;
import com.ppaw.dataaccess.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(UUID id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @Transactional
    public User updateUser(UUID id, String email, User.UserRole role) {
        log.info("Updating user: {}", id);
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (email != null && !email.isEmpty()) {
            user.setEmail(email);
        }
        if (role != null) {
            user.setRole(role);
        }
        
        user = userRepository.save(user);
        log.info("User updated successfully: {}", id);
        return user;
    }

    @Transactional
    public void updateUserPassword(UUID id, String password) {
        log.info("Updating password for user: {}", id);
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setPasswordHash(password);
        userRepository.save(user);
        log.info("Password updated for user: {}", id);
    }
}
