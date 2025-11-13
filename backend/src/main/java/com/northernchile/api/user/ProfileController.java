package com.northernchile.api.user;

import com.northernchile.api.config.security.annotation.CurrentUser;
import com.northernchile.api.model.User;
import com.northernchile.api.user.dto.PasswordChangeReq;
import com.northernchile.api.user.dto.ProfileUpdateReq;
import com.northernchile.api.user.dto.UserRes;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserService userService;
    private final UserRepository userRepository;

    public ProfileController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<UserRes> getCurrentUserProfile(@CurrentUser User currentUser) {
        UserRes userRes = userService.mapToUserRes(currentUser);
        return ResponseEntity.ok(userRes);
    }

    @PutMapping("/me")
    public ResponseEntity<UserRes> updateCurrentUserProfile(
            @CurrentUser User currentUser,
            @Valid @RequestBody ProfileUpdateReq req) {
        UserRes updatedUser = userService.updateUserProfile(currentUser, req);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/me/password")
    public ResponseEntity<Map<String, String>> changePassword(
            Authentication authentication,
            @Valid @RequestBody PasswordChangeReq req) {
        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Usuario no encontrado"));

        try {
            userService.changeUserPassword(currentUser, req.getCurrentPassword(), req.getNewPassword());
            return ResponseEntity.ok(Map.of("message", "Contraseña actualizada correctamente."));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
