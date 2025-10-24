package com.northernchile.api.auth;

import com.northernchile.api.auth.dto.AuthRes;
import com.northernchile.api.auth.dto.LoginReq;
import com.northernchile.api.auth.dto.RegisterReq;
import com.northernchile.api.config.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginReq loginReq) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginReq.getEmail(), loginReq.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtil.generateToken(authentication.getName());

        return ResponseEntity.ok(new AuthRes(jwt));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterReq registerReq) {
        authService.register(registerReq);
        return ResponseEntity.ok("User registered successfully");
    }
}
