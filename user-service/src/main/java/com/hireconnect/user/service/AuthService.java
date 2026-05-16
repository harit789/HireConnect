package com.hireconnect.user.service;

import java.util.Map;

import com.hireconnect.user.entity.UserCredential;

public interface AuthService {

    Map<String, String> register(String email, String password, String role, String fullName);

    Map<String, String> login(String email, String password);

    boolean validateToken(String token);

    Map<String, String> refreshToken(String token);

    Map<String, String> logout(String token);

    UserCredential getByEmail(String email);

    UserCredential getById(Long userId);

    void deleteUser(Long userId);
}
