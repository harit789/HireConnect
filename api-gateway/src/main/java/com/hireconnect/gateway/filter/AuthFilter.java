package com.hireconnect.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.List;

@Component
public class AuthFilter extends AbstractGatewayFilterFactory<AuthFilter.Config> {

    @Value("${jwt.secret}")
    private String jwtSecret;

   
    private static final List<String> OPEN_ENDPOINTS = List.of(
        "/api/auth/register",
        "/api/auth/login",
        "/api/auth/verify-otp",
        "/api/auth/forgot-password",
        "/api/auth/reset-password",
        "/api/auth/refresh",
        "/api/auth/validate",

        // OAuth2 callback and initiation — must reach user-service without JWT
        "/login/oauth2/",
        "/oauth2/authorization/",

        // Swagger / API docs — no auth needed to read docs
        "/swagger-ui",
        "/webjars",
        "/user-service/api-docs",
        "/job-service/api-docs",
        "/interview-service/api-docs",
        "/subscription-service/api-docs",
        "/api/subscriptions/plan-info"
    );

    public AuthFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // If this route is marked as unsecured (secured: false in yaml), skip JWT
            if (!config.isSecured()) {
                return chain.filter(exchange);
            }

            String path   = exchange.getRequest().getPath().toString();
            HttpMethod method = exchange.getRequest().getMethod();

            // Check open endpoints (exact prefix match — not startsWith on short paths)
            boolean isOpen = OPEN_ENDPOINTS.stream().anyMatch(path::startsWith);
            if (isOpen) return chain.filter(exchange);

            // BUG FIX: Public job browsing — allow GET requests to /api/jobs
            // without requiring a JWT token. POST/PUT/PATCH/DELETE still need auth.
            if (path.startsWith("/api/jobs") && HttpMethod.GET.equals(method)) {
                // Still forward if token present, so downstream gets X-User-Id
                // for personalised responses; but don't reject if absent.
                String authHeader = exchange.getRequest()
                    .getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    return chain.filter(exchange); // unauthenticated browse — OK
                }
                // Token present — validate and forward user info, then continue
                return validateAndForward(exchange, chain, authHeader.substring(7));
            }

            // All other routes: require valid JWT
            if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            String authHeader = exchange.getRequest()
                .getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            return validateAndForward(exchange, chain, authHeader.substring(7));
        };
    }

    private Mono<Void> validateAndForward(ServerWebExchange exchange,
                                          org.springframework.cloud.gateway.filter.GatewayFilterChain chain,
                                          String token) {
        try {
            Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(key).build()
                .parseClaimsJws(token).getBody();

            // Forward user identity to downstream services as trusted headers
            ServerWebExchange modifiedExchange = exchange.mutate()
                .request(r -> r
                    .header("X-User-Id",   claims.getSubject())
                    .header("X-User-Role", claims.get("role", String.class)))
                .build();

            return chain.filter(modifiedExchange);

        } catch (Exception e) {
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }

    public static class Config {
        private boolean secured = true;
        public boolean isSecured() { return secured; }
        public void setSecured(boolean secured) { this.secured = secured; }
    }
}
