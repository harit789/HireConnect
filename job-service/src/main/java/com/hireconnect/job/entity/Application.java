package com.hireconnect.job.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
    name = "applications",
    uniqueConstraints = @UniqueConstraint(columnNames = {"job_id", "candidate_id"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long applicationId;

    @Column(name = "job_id", nullable = false)
    private Long jobId;

    @Column(name = "candidate_id", nullable = false)
    private Long candidateId;

    @Column(updatable = false)
    private LocalDate appliedAt;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(length = 2000)
    private String coverLetter;

    @Column(length = 500)
    private String resumeUrl;

    @Column(length = 500)
    private String recruiterNote;

    @PrePersist
    public void prePersist() {
        this.appliedAt = LocalDate.now();
        if (this.status == null) {
            this.status = "APPLIED";
        }
    }
}
