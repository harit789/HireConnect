package com.hireconnect.user.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "candidate_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateProfile implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long profileId;

    @Column(nullable = false)
    private Long userId;

    @Column(length = 100)
    private String fullName;

    @Column(length = 150)
    private String email;

    private Long mobile;

    private LocalDate dob;

    @Column(length = 10)
    private String gender;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "candidate_skills",
        joinColumns = @JoinColumn(name = "profile_id")
    )
    @Column(name = "skill", length = 60)
    private List<String> skills;

    private Integer experience;

    @Column(length = 500)
    private String resumeUrl;

    @Column(length = 1000)
    private String summary;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "candidate_saved_jobs",
        joinColumns = @JoinColumn(name = "profile_id")
    )
    @Column(name = "job_id")
    private List<Long> savedJobs;

    @Embedded
    private Address address;
}
