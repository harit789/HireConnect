package com.hireconnect.user.resource;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hireconnect.user.service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthResource {

    private final AuthService authService;

  
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody Map<String, String> body) {
        String email    = body.get("email");
        String password = body.get("password");
        String role     = body.getOrDefault("role", "CANDIDATE");
        String fullName = body.get("fullName");
        return ResponseEntity.ok(authService.register(email, password, role, fullName));
    }


    
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(authService.login(body.get("email"), body.get("password")));
    }

   
    @GetMapping("/validate")
    public ResponseEntity<Map<String, Boolean>> validate(@RequestParam String token) {
        return ResponseEntity.ok(Map.of("valid", authService.validateToken(token)));
    }

    
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh(@RequestParam String token) {
        return ResponseEntity.ok(authService.refreshToken(token));
    }

  
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestParam(required = false, defaultValue = "") String token) {
        return ResponseEntity.ok(authService.logout(token));
    }

  
    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long userId) {
        authService.deleteUser(userId);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }

}
