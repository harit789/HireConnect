package com.hireconnect.user.resource;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hireconnect.user.entity.UserCredential;
import com.hireconnect.user.repository.UserCredentialRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminResource {

    private final UserCredentialRepository userRepo;

    @GetMapping("/users")
    public ResponseEntity<List<UserCredential>> getAllUsers() {
        return ResponseEntity.ok(userRepo.findAll());
    }

    @PatchMapping("/users/{userId}/suspend")
    public ResponseEntity<Map<String, String>> suspendUser(@PathVariable Long userId) {
        UserCredential user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        user.setIsActive(false);
        userRepo.save(user);
        
        return ResponseEntity.ok(Map.of("message", "User suspended successfully"));
    }

    @PatchMapping("/users/{userId}/activate")
    public ResponseEntity<Map<String, String>> activateUser(@PathVariable Long userId) {
        UserCredential user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        user.setIsActive(true);
        userRepo.save(user);
        
        return ResponseEntity.ok(Map.of("message", "User activated successfully"));
    }
}
