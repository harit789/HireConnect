package com.hireconnect.interview.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "interviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long interviewId;

    @Column(nullable = false)
    private Long applicationId;

    @Column(nullable = false)
    private Long candidateId;

    @Column(nullable = false)
    private Long recruiterId;

    @Column(nullable = false)
    private LocalDateTime scheduledAt;

    @Column(nullable = false, length = 20)
    private String mode;

    @Column(length = 300)
    private String meetLink;

    @Column(length = 300)
    private String location;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(length = 1000)
    private String notes;

    @Column(length = 100)
    private String candidateEmail;

    @Column(length = 100)
    private String recruiterEmail;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "SCHEDULED";
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
