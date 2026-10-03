package com.danielnavia.melodygenerator.controller;

import com.danielnavia.melodygenerator.dto.auth.AuthResponse;
import com.danielnavia.melodygenerator.dto.auth.LoginRequest;
import com.danielnavia.melodygenerator.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {this.authService = authService;}

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {

        AuthResponse response = authService.login(request);
        if (response == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(response);
    }
}