package com.hireconnect.job.repository;

import com.hireconnect.job.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByCandidateId(Long candidateId);

    List<Application> findByJobId(Long jobId);

    List<Application> findByJobIdAndStatus(Long jobId, String status);

    List<Application> findByCandidateIdAndStatus(Long candidateId, String status);

    Optional<Application> findByJobIdAndCandidateId(Long jobId, Long candidateId);

    boolean existsByJobIdAndCandidateId(Long jobId, Long candidateId);

    long countByJobId(Long jobId);

    long countByJobIdAndStatus(Long jobId, String status);

    void deleteByJobId(Long jobId);
}
