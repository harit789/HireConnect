package com.hireconnect.job.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "jobs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long jobId;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 80)
    private String category;

    @Column(nullable = false, length = 50)
    private String type;

    @Column(nullable = false, length = 100)
    private String location;

    private Double salaryMin;
    private Double salaryMax;

    @Column(length = 3000)
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "job_skills",
        joinColumns = @JoinColumn(name = "job_id")
    )
    @Column(name = "skill", length = 60)
    private List<String> skills;

    private Integer experienceRequired;

    @Column(nullable = false)
    private Long postedBy;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(updatable = false)
    private LocalDate postedAt;

    private LocalDate deadline;

    @Column(length = 200)
    private String companyName;

    @PrePersist
    public void prePersist() {
        this.postedAt = LocalDate.now();
        if (this.status == null) {
            this.status = "ACTIVE";
        }
    }
}
