package com.danielnavia.melodygenerator.service;

import com.danielnavia.melodygenerator.dto.auth.AuthResponse;
import com.danielnavia.melodygenerator.dto.auth.LoginRequest;
import com.danielnavia.melodygenerator.entity.RoleEntity;
import com.danielnavia.melodygenerator.entity.UserEntity;
import com.danielnavia.melodygenerator.model.Role;
import com.danielnavia.melodygenerator.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final UserRepository userRepository;

    @Value("${app.jwt.expires-in}")
    private long expiresIn;


    public AuthResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user != null) {
            boolean ok = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());
            if (ok) {
                List<Role> roles = user.getRoles().stream().map(RoleEntity::getName).toList();
                String token = tokenService.generateToken(user.getId(), user.getEmail(), roles);
                return new AuthResponse(token, "Bearer", expiresIn);
            }else return null;
        }
        else return null;
    }


}
