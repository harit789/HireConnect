package com.hireconnect.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.web.SecurityFilterChain;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            // CRITICAL FIX: OAuth2 requires sessions to store the 'state' parameter
            // between the initial redirect to GitHub and the callback.
            // STATELESS breaks this — use IF_REQUIRED so sessions are created only
            // when needed (i.e., during the OAuth2 flow).
            .sessionManagement(sm ->
                sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints — auth and profile are open
                // (JWT validation is done at the API Gateway level)
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/profile/**").permitAll()
                .requestMatchers("/login/oauth2/**").permitAll()
                .requestMatchers("/oauth2/**").permitAll()
                // Allow public access to uploaded resume files
                .requestMatchers("/uploads/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth -> oauth
                // Explicitly use HttpSession to persist the OAuth2 authorization
                // request (including the 'state' nonce) across the GitHub redirect.
                .authorizationEndpoint(ep -> ep
                    .authorizationRequestRepository(
                        new HttpSessionOAuth2AuthorizationRequestRepository()))
                .successHandler(oAuth2SuccessHandler)
            );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
