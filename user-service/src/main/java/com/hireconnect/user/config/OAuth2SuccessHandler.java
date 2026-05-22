package com.hireconnect.user.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.hireconnect.user.entity.UserCredential;
import com.hireconnect.user.repository.UserCredentialRepository;
import com.hireconnect.user.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserCredentialRepository userRepo;
    private final JwtUtil jwtUtil;

    // BUG FIX: use configurable frontend URL, defaulting to Angular's port 4200
    @Value("${app.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    public OAuth2SuccessHandler(UserCredentialRepository userRepo, JwtUtil jwtUtil) {
        this.userRepo = userRepo;
        this.jwtUtil  = jwtUtil;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();

        // GitHub may return null email if user has private email set — fall back to login
        String email = oauthUser.getAttribute("email");
        if (email == null) {
            String login = oauthUser.getAttribute("login");
            email = login + "@github.com";
        }

        final String finalEmail = email;

        UserCredential user = userRepo.findByEmail(finalEmail).orElseGet(() -> {
            UserCredential newUser = UserCredential.builder()
                    .email(finalEmail)
                    .role("CANDIDATE")
                    .provider("GITHUB")
                    .build();
            return userRepo.save(newUser);
        });

        String token = jwtUtil.generateToken(user.getUserId().toString(), user.getRole());

        // Redirect to Angular /oauth-success route with token in query params
        response.sendRedirect(frontendUrl + "/oauth-success?token=" + token
                + "&role=" + user.getRole()
                + "&userId=" + user.getUserId());
    }
}
