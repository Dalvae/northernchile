package com.northernchile.api.auth;

import com.northernchile.api.auth.dto.RegisterReq;
import com.northernchile.api.model.User;
import com.northernchile.api.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User register(RegisterReq registerReq) {
        if (userRepository.findByEmail(registerReq.getEmail()).isPresent()) {
            throw new RuntimeException("Email already in use");
        }

        User user = new User();
        user.setEmail(registerReq.getEmail());
        user.setFullName(registerReq.getFullName());
        user.setPasswordHash(passwordEncoder.encode(registerReq.getPassword()));
        user.setRole("ROLE_CLIENT"); // Default role
        user.setAuthProvider("LOCAL");

        return userRepository.save(user);
    }
}
