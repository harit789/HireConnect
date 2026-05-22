package com.hireconnect.user.service;

import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hireconnect.user.entity.UserCredential;
import com.hireconnect.user.repository.UserCredentialRepository;
import com.hireconnect.user.util.JwtUtil;

import com.hireconnect.user.repository.CandidateProfileRepository;
import com.hireconnect.user.repository.RecruiterProfileRepository;
import com.hireconnect.user.entity.CandidateProfile;
import com.hireconnect.user.entity.RecruiterProfile;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserCredentialRepository userRepo;
    private final CandidateProfileRepository candidateRepo;
    private final RecruiterProfileRepository recruiterRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    @Override
    @Transactional
    public Map<String, String> register(String email, String password, String role, String fullName) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new RuntimeException("Invalid email format");
        }
        
        if (userRepo.existsByEmail(email)) {
            UserCredential existing = userRepo.findByEmail(email).get();
            if (existing.getIsActive() != null && existing.getIsActive()) {
                throw new RuntimeException("Email already registered and active: " + email);
            }

            existing.setPasswordHash(passwordEncoder.encode(password));
            existing.setRole(role.toUpperCase());
            existing.setIsActive(true);
            userRepo.save(existing);
            
            String token = jwtUtil.generateToken(existing.getUserId().toString(), existing.getRole());
            
            createProfileIfNotExists(existing, fullName);
            
            return Map.of(
                "token", token,
                "role", existing.getRole(),
                "userId", existing.getUserId().toString(),
                "email", existing.getEmail(),
                "message", "Registration successful."
            );
        }
        
        UserCredential user = UserCredential.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .role(role.toUpperCase())
                .provider("LOCAL")
                .isActive(true)
                .build();

        UserCredential saved = userRepo.save(user);

        String token = jwtUtil.generateToken(saved.getUserId().toString(), saved.getRole());

        createProfileIfNotExists(saved, fullName);

        return Map.of(
                "token", token,
                "role", saved.getRole(),
                "userId", saved.getUserId().toString(),
                "email", saved.getEmail(),
                "message", "Registration successful."
        );
    }

    private void createProfileIfNotExists(UserCredential user, String fullName) {
        if ("CANDIDATE".equalsIgnoreCase(user.getRole())) {
            if (candidateRepo.findByUserId(user.getUserId()).isEmpty()) {
                candidateRepo.save(CandidateProfile.builder()
                        .userId(user.getUserId())
                        .email(user.getEmail())
                        .fullName(fullName != null ? fullName : "Job Seeker")
                        .build());
            }
        } else if ("RECRUITER".equalsIgnoreCase(user.getRole())) {
            if (recruiterRepo.findByUserId(user.getUserId()).isEmpty()) {
                recruiterRepo.save(RecruiterProfile.builder()
                        .userId(user.getUserId())
                        .email(user.getEmail())
                        .fullName(fullName != null ? fullName : "Recruiter")
                        .build());
            }
        }
    }

    @Override
    public Map<String, String> login(String email, String password) {
        UserCredential user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No account found for: " + email));

        if ("GITHUB".equals(user.getProvider())) {
            throw new RuntimeException("This account uses GitHub login. Please use 'Continue with GitHub'.");
        }
        
        if (user.getIsActive() != null && !user.getIsActive()) {
            throw new RuntimeException("Account is not active. Please verify your email or contact admin.");
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("Invalid password");
        }

        String token = jwtUtil.generateToken(user.getUserId().toString(), user.getRole());
        return Map.of(
                "token", token,
                "role", user.getRole(),
                "userId", user.getUserId().toString(),
                "email", user.getEmail(),
                "message", "Login successful"
        );
    }

    @Override
    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }

    @Override
    public Map<String, String> refreshToken(String token) {
        if (!jwtUtil.validateToken(token)) {
            throw new RuntimeException("Token is invalid or expired");
        }
        String userId = jwtUtil.extractUserId(token);
        String role   = jwtUtil.extractRole(token);
        String newToken = jwtUtil.generateToken(userId, role);
        return Map.of("token", newToken, "message", "Token refreshed");
    }

    @Override
    public Map<String, String> logout(String token) {

        return Map.of("message", "Logout successful");
    }

    @Override
    @Cacheable(value = "userByEmail", key = "#email")
    public UserCredential getByEmail(String email) {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }

    @Override
    @Cacheable(value = "userById", key = "#userId")
    public UserCredential getById(Long userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
    }

    @Override
    @Transactional
    @CacheEvict(value = {"userById", "userByEmail"}, allEntries = true)
    public void deleteUser(Long userId) {
        userRepo.deleteByUserId(userId);
    }

}
