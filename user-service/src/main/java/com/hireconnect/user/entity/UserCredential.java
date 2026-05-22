package com.hireconnect.user.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_credentials")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(unique = true, nullable = false, length = 150)
    private String email;

    private String passwordHash;

    @Column(nullable = false, length = 20)
    private String role;        // CANDIDATE | RECRUITER | ADMIN

    @Column(length = 20)
    private String provider;    // LOCAL | GITHUB

    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.isActive == null) {
            this.isActive = true;
        }
    }
}
