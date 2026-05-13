package com.eaglebank.backend.controller;

import com.eaglebank.backend.dto.AuthResponse;
import com.eaglebank.backend.dto.CreateUserRequest;
import com.eaglebank.backend.dto.CreatedResponse;
import com.eaglebank.backend.dto.ErrorResponse;
import com.eaglebank.backend.dto.LoginRequest;
import com.eaglebank.backend.model.User;
import com.eaglebank.backend.security.JwtService;
import com.eaglebank.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        User user = userService.findByEmail(loginRequest.getEmail());
        if (user != null && passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            String token = jwtService.generateToken(user.getId());
            return ResponseEntity.ok(new AuthResponse(token));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("Invalid credentials"));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody CreateUserRequest request) {
        User existing = userService.findByEmail(request.getEmail());
        if (existing != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse("Email already in use"));
        }

        var created = userService.registerUser(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreatedResponse("User created successfully", created.getId()));
    }
}