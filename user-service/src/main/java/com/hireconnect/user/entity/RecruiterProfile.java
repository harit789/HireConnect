package com.hireconnect.user.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "recruiter_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecruiterProfile implements Serializable {

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

    @Column(length = 150)
    private String companyName;

    @Column(length = 50)
    private String companySize;

    @Column(length = 80)
    private String industry;

    @Column(length = 200)
    private String website;

    @Column(length = 80)
    private String designation;

    @Embedded
    private Address address;

    private Long teamId; // Used for recruiter team management / co-managing jobs
}
