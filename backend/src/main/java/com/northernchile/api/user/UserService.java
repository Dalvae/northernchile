
package com.northernchile.api.user;

import com.northernchile.api.audit.AuditLogService;
import com.northernchile.api.model.User;
import com.northernchile.api.user.dto.UserCreateReq;
import com.northernchile.api.user.dto.UserRes;
import com.northernchile.api.user.dto.UserUpdateReq;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuditLogService auditLogService;

    public List<UserRes> getAllUsers() {
        return userRepository.findAll().stream()
                .filter(user -> user.getDeletedAt() == null) // Exclude soft-deleted users
                .map(this::toUserRes)
                .collect(Collectors.toList());
    }

    public Optional<UserRes> getUserById(UUID userId) {
        return userRepository.findById(userId)
                .filter(user -> user.getDeletedAt() == null) // Exclude soft-deleted users
                .map(this::toUserRes);
    }

    @Transactional
    public UserRes createUser(UserCreateReq req) {
        // Check if email already exists
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            throw new IllegalStateException("Email already exists: " + req.getEmail());
        }

        User user = new User();
        user.setEmail(req.getEmail());
        user.setFullName(req.getFullName());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setRole(req.getRole());
        user.setNationality(req.getNationality());
        user.setPhoneNumber(req.getPhoneNumber());
        user.setAuthProvider("LOCAL");

        User savedUser = userRepository.save(user);

        // Audit log - only SUPER_ADMIN can create users, so we use savedUser as currentUser
        Map<String, Object> newValues = Map.of(
            "id", savedUser.getId().toString(),
            "email", savedUser.getEmail(),
            "fullName", savedUser.getFullName(),
            "role", savedUser.getRole()
        );
        auditLogService.logCreate(savedUser, "USER", savedUser.getId(), savedUser.getEmail(), newValues);

        return toUserRes(savedUser);
    }

    @Transactional
    public UserRes updateUser(UUID userId, UserUpdateReq req, User currentUser) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        if (user.getDeletedAt() != null) {
            throw new IllegalStateException("Cannot update a deleted user");
        }

        // Capture old values for audit
        Map<String, Object> oldValues = Map.of(
            "fullName", user.getFullName() != null ? user.getFullName() : "",
            "role", user.getRole() != null ? user.getRole() : "",
            "nationality", user.getNationality() != null ? user.getNationality() : ""
        );

        if (req.getFullName() != null) {
            user.setFullName(req.getFullName());
        }
        if (req.getRole() != null) {
            user.setRole(req.getRole());
        }
        if (req.getNationality() != null) {
            user.setNationality(req.getNationality());
        }
        if (req.getPhoneNumber() != null) {
            user.setPhoneNumber(req.getPhoneNumber());
        }
        if (req.getDateOfBirth() != null) {
            user.setDateOfBirth(req.getDateOfBirth());
        }

        User updatedUser = userRepository.save(user);

        // Audit log
        Map<String, Object> newValues = Map.of(
            "fullName", updatedUser.getFullName() != null ? updatedUser.getFullName() : "",
            "role", updatedUser.getRole() != null ? updatedUser.getRole() : "",
            "nationality", updatedUser.getNationality() != null ? updatedUser.getNationality() : ""
        );
        auditLogService.logUpdate(currentUser, "USER", updatedUser.getId(), updatedUser.getEmail(), oldValues, newValues);

        return toUserRes(updatedUser);
    }

    @Transactional
    public void deleteUser(UUID userId, User currentUser) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        if (user.getDeletedAt() != null) {
            throw new IllegalStateException("User is already deleted");
        }

        // Soft delete
        user.setDeletedAt(Instant.now());
        userRepository.save(user);

        // Audit log
        Map<String, Object> oldValues = Map.of(
            "email", user.getEmail(),
            "fullName", user.getFullName(),
            "role", user.getRole(),
            "deletedAt", user.getDeletedAt().toString()
        );
        auditLogService.logDelete(currentUser, "USER", user.getId(), user.getEmail(), oldValues);
    }

    private UserRes toUserRes(User user) {
        UserRes res = new UserRes();
        res.setId(user.getId());
        res.setEmail(user.getEmail());
        res.setFullName(user.getFullName());
        res.setRole(user.getRole());
        res.setNationality(user.getNationality());
        res.setPhoneNumber(user.getPhoneNumber());
        res.setDateOfBirth(user.getDateOfBirth());
        res.setAuthProvider(user.getAuthProvider());
        res.setCreatedAt(user.getCreatedAt());
        return res;
    }
}
