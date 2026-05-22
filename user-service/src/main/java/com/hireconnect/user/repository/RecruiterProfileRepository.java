package com.hireconnect.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hireconnect.user.entity.RecruiterProfile;

@Repository
public interface RecruiterProfileRepository extends JpaRepository<RecruiterProfile, Long> {

    Optional<RecruiterProfile> findByUserId(Long userId);

    Optional<RecruiterProfile> findByEmail(String email);
}
