package com.railway.auth.service;

import com.railway.auth.dto.LoginRequest;
import com.railway.auth.dto.UserRequest;
import com.railway.auth.entity.Role;
import com.railway.auth.entity.User;
import com.railway.auth.repository.UserRepository;
import com.railway.auth.util.JwtUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public String register(UserRequest request) {
        User user = new User();
        user.setUsername(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        userRepository.save(user);
        return user.getUsername() + " registered successfully";
    }

    public String login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid password");
        }

        return jwtUtil.generateToken(user.getEmail(), user.getRole().name());
    }

    @PostConstruct
    public void createAdmin() {

        if (!userRepository.findByEmail("sushant@test.com").isPresent()) {
            User admin = new User();
            admin.setUsername("Sushant");
            admin.setEmail("sushant@test.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);

            userRepository.save(admin);
        }
    }
}