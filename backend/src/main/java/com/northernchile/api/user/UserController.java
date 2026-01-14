package com.northernchile.api.user;

import com.northernchile.api.config.security.annotation.CurrentUser;
import com.northernchile.api.model.User;
import com.northernchile.api.security.Permission;
import com.northernchile.api.security.annotations.RequiresPermission;
import com.northernchile.api.user.dto.AdminPasswordChangeReq;
import com.northernchile.api.user.dto.UserCreateReq;
import com.northernchile.api.user.dto.UserRes;
import com.northernchile.api.user.dto.UserUpdateReq;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@RequiresPermission(Permission.MANAGE_USERS) // Super Admin only
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    public UserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    /**
     * Get paginated list of all users.
     * Supports pagination via ?page=0&size=20&sort=createdAt,desc
     */
    @GetMapping
    public ResponseEntity<Page<UserRes>> getAllUsers(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsersPaged(pageable));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserRes> getUserById(@PathVariable UUID userId) {
        return userService.getUserById(userId)
                .map(user -> new ResponseEntity<>(user, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    @RequiresPermission(Permission.CREATE_USER)
    public ResponseEntity<UserRes> createUser(@Valid @RequestBody UserCreateReq req) {
        UserRes createdUser = userService.createUser(req);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @PutMapping("/{userId}")
    @RequiresPermission(Permission.EDIT_USER)
    public ResponseEntity<UserRes> updateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody UserUpdateReq req,
            @CurrentUser User currentUser) {
        UserRes updatedUser = userService.updateUser(userId, req, currentUser);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    @DeleteMapping("/{userId}")
    @RequiresPermission(Permission.DELETE_USER)
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId,
                                           @CurrentUser User currentUser) {
        userService.deleteUser(userId, currentUser);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{userId}/password")
    @RequiresPermission(Permission.MANAGE_USERS)
    public ResponseEntity<Map<String, String>> adminChangePassword(
            @PathVariable UUID userId,
            @Valid @RequestBody AdminPasswordChangeReq req,
            @CurrentUser User currentUser) {
        try {
            userService.adminResetUserPassword(currentUser, userId, req.newPassword());
            return ResponseEntity.ok(Map.of("message", "Contraseña del usuario actualizada correctamente."));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }
}
