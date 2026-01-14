package com.ppaw.service;

import com.ppaw.dataaccess.entity.User;
import com.ppaw.dataaccess.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    @Cacheable(value = "users", key = "'all'")
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        log.info("Fetching all users");
        return userRepository.findByIsDeletedFalse();
    }

    @Cacheable(value = "users", key = "#id")
    @Transactional(readOnly = true)
    public User getUserById(UUID id) {
        log.info("Fetching user by id: {}", id);
        return userRepository.findByIdAndIsDeletedFalse(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @CacheEvict(value = "users", allEntries = true)
    @Transactional
    public User updateUser(UUID id, String email, User.UserRole role) {
        log.info("Updating user: {}", id);
        User user = userRepository.findByIdAndIsDeletedFalse(id)
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

    @CacheEvict(value = "users", allEntries = true)
    @Transactional
    public void updateUserPassword(UUID id, String password) {
        log.info("Updating password for user: {}", id);
        User user = userRepository.findByIdAndIsDeletedFalse(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setPasswordHash(password);
        userRepository.save(user);
        log.info("Password updated for user: {}", id);
    }

    @CacheEvict(value = "users", allEntries = true)
    @Transactional
    public void softDeleteUser(UUID id) {
        log.info("Soft deleting user: {}", id);
        User user = userRepository.findByIdAndIsDeletedFalse(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setIsDeleted(true);
        userRepository.save(user);
        log.info("User soft deleted successfully: {}", id);
    }

    @CacheEvict(value = "users", allEntries = true)
    @Transactional
    public void hardDeleteUser(UUID id) {
        log.info("Hard deleting user: {}", id);
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        userRepository.delete(user);
        log.info("User hard deleted successfully: {}", id);
    }
}
