package com.api.playpal.auth.application;

import com.api.playpal.auth.infrastructure.JwtUtil;
import com.api.playpal.user.application.UserService;
import com.api.playpal.user.domain.User;
import org.bson.types.Binary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Service
public class AuthService {
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    public String login(String email, String password) {
        Optional<User> user = userService.findByEmail(email);
        if (user.isPresent() && passwordEncoder.matches(password, user.get().getPassword())) {
            String token = jwtUtil.generateToken(user.get().getId());
            return token;
        }
        throw new RuntimeException("Invalid email or password");
    }

    public User register(String username, String email, String password, String role, MultipartFile img) throws IOException {
        User user = new User(username, email, passwordEncoder.encode(password), role);
        return userService.save(user, img);
    }
}
