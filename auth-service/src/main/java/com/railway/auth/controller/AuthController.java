package com.railway.auth.controller;

import com.railway.auth.dto.LoginRequest;
import com.railway.auth.dto.UserRequest;
import com.railway.auth.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public String register(@Valid @RequestBody UserRequest request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    public String login(@Valid @RequestBody LoginRequest request) {
        return userService.login(request);
    }
}