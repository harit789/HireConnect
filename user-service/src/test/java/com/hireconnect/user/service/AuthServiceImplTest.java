package com.hireconnect.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.hireconnect.user.entity.UserCredential;
import com.hireconnect.user.repository.UserCredentialRepository;
import com.hireconnect.user.util.JwtUtil;
import com.hireconnect.user.repository.CandidateProfileRepository;
import com.hireconnect.user.repository.RecruiterProfileRepository;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private UserCredentialRepository userRepo;

    @Mock
    private CandidateProfileRepository candidateRepo;

    @Mock
    private RecruiterProfileRepository recruiterRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    private UserCredential mockUser;

    @BeforeEach
    void setUp() {
        mockUser = UserCredential.builder()
                .userId(1L)
                .email("test@example.com")
                .passwordHash("encodedPassword")
                .role("JOB_SEEKER")
                .provider("LOCAL")
                .isActive(true)
                .build();
    }

    @Test
    void register_Success() {
        when(userRepo.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        
        UserCredential savedUser = UserCredential.builder()
                .userId(1L)
                .email("test@example.com")
                .role("JOB_SEEKER")
                .build();
        when(userRepo.save(any(UserCredential.class))).thenReturn(savedUser);
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("mockToken");

        Map<String, String> response = authService.register("test@example.com", "password", "JOB_SEEKER", "Test User");

        assertNotNull(response);
        assertEquals("mockToken", response.get("token"));
        assertEquals("JOB_SEEKER", response.get("role"));
        verify(userRepo, times(1)).save(any(UserCredential.class));
    }

    @Test
    void register_EmailExists_ThrowsException() {
        when(userRepo.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> 
            authService.register("test@example.com", "password", "JOB_SEEKER", "Test User")
        );
        verify(userRepo, never()).save(any());
    }

    @Test
    void login_Success() {
        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken("1", "JOB_SEEKER")).thenReturn("mockToken");

        Map<String, String> response = authService.login("test@example.com", "password");

        assertNotNull(response);
        assertEquals("mockToken", response.get("token"));
        assertEquals("JOB_SEEKER", response.get("role"));
    }

    @Test
    void login_InvalidPassword_ThrowsException() {
        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> 
            authService.login("test@example.com", "wrongpassword")
        );
    }

    @Test
    void login_UserSuspended_ThrowsException() {
        mockUser.setIsActive(false);
        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));

        assertThrows(RuntimeException.class, () -> 
            authService.login("test@example.com", "password")
        );
    }

    @Test
    void login_GithubProvider_ThrowsException() {
        mockUser.setProvider("GITHUB");
        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));

        assertThrows(RuntimeException.class, () -> 
            authService.login("test@example.com", "password")
        );
    }

    @Test
    void refreshToken_Success() {
        when(jwtUtil.validateToken("oldToken")).thenReturn(true);
        when(jwtUtil.extractUserId("oldToken")).thenReturn("1");
        when(jwtUtil.extractRole("oldToken")).thenReturn("JOB_SEEKER");
        when(jwtUtil.generateToken("1", "JOB_SEEKER")).thenReturn("newToken");

        Map<String, String> response = authService.refreshToken("oldToken");

        assertNotNull(response);
        assertEquals("newToken", response.get("token"));
    }

    @Test
    void refreshToken_InvalidToken_ThrowsException() {
        when(jwtUtil.validateToken("invalidToken")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> 
            authService.refreshToken("invalidToken")
        );
    }
}
