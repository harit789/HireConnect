package com.hireconnect.interview.repository;

import com.hireconnect.interview.entity.Interview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {

    List<Interview> findByApplicationId(Long applicationId);

    List<Interview> findByCandidateId(Long candidateId);

    List<Interview> findByRecruiterId(Long recruiterId);

    List<Interview> findByStatus(String status);

    List<Interview> findByCandidateIdAndStatus(Long candidateId, String status);

    List<Interview> findByRecruiterIdAndStatus(Long recruiterId, String status);

    List<Interview> findByScheduledAtBetween(LocalDateTime from, LocalDateTime to);

    Optional<Interview> findByApplicationIdAndStatus(Long applicationId, String status);

    void deleteByApplicationId(Long applicationId);
}
