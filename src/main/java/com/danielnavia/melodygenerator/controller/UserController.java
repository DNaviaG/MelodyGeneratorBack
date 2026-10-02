package com.danielnavia.melodygenerator.controller;

import com.danielnavia.melodygenerator.dto.user.UserRequest;
import com.danielnavia.melodygenerator.dto.user.UserResponse;
import com.danielnavia.melodygenerator.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> save(@Valid @RequestBody UserRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(userService.save(request));
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> findAll() {

        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findById(@PathVariable Integer id) {

        UserResponse user = userService.findById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserResponse> partialUpdate(@PathVariable  Integer id, @Valid @RequestBody UserRequest request) {

        UserResponse user = userService.partialUpdate(id, request);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Integer id) {

        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}