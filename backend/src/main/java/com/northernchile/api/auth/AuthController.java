package com.northernchile.api.auth;

import com.northernchile.api.auth.dto.LoginReq;
import com.northernchile.api.auth.dto.RegisterReq;
import com.northernchile.api.config.security.JwtUtil;
import com.northernchile.api.model.User;
import com.northernchile.api.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginReq loginReq) {
        return ResponseEntity.ok(authService.login(loginReq));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterReq registerReq) {
        authService.register(registerReq);
        return ResponseEntity.ok("User registered successfully");
    }
}