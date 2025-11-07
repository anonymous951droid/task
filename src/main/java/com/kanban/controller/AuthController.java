package com.kanban.controller;

import com.kanban.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication API")
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Simple login endpoint that returns a JWT token")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest request) {
        // Simple authentication - in production, validate against user database
        String token = jwtTokenProvider.generateToken(request.getUsername());
        return ResponseEntity.ok(Map.of("token", token));
    }

    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }
}

