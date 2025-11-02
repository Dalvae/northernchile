package com.northernchile.api.user;

import com.northernchile.api.model.User;
import com.northernchile.api.user.dto.AdminPasswordChangeReq;
import com.northernchile.api.user.dto.UserCreateReq;
import com.northernchile.api.user.dto.UserRes;
import com.northernchile.api.user.dto.UserUpdateReq;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping
    public ResponseEntity<List<UserRes>> getAllUsers() {
        List<UserRes> users = userService.getAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/{userId}")
    public ResponseEntity<UserRes> getUserById(@PathVariable UUID userId) {
        return userService.getUserById(userId)
                .map(user -> new ResponseEntity<>(user, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping
    public ResponseEntity<UserRes> createUser(@Valid @RequestBody UserCreateReq req) {
        UserRes createdUser = userService.createUser(req);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping("/{userId}")
    public ResponseEntity<UserRes> updateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody UserUpdateReq req) {
        User currentUser = getCurrentUser();
        UserRes updatedUser = userService.updateUser(userId, req, currentUser);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
        User currentUser = getCurrentUser();
        userService.deleteUser(userId, currentUser);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping("/{userId}/password")
    public ResponseEntity<Map<String, String>> adminChangePassword(
            @PathVariable UUID userId,
            @Valid @RequestBody AdminPasswordChangeReq req) {
        User currentUser = getCurrentUser();
        try {
            userService.adminResetUserPassword(currentUser, userId, req.getNewPassword());
            return ResponseEntity.ok(Map.of("message", "Contraseña del usuario actualizada correctamente."));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        return userRepository.findByEmail(currentPrincipalName)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
