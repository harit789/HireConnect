package com.hireconnect.job.service;

import com.hireconnect.job.entity.Application;

import java.util.List;

public interface ApplicationService {

    Application submitApplication(Application application);

    Application getApplicationById(Long applicationId);

    List<Application> getByCandidate(Long candidateId);

    List<Application> getByJob(Long jobId);

    List<Application> getByJobAndStatus(Long jobId, String status);

    Application updateStatus(Long applicationId, String status, String recruiterNote);

    void withdrawApplication(Long applicationId, Long candidateId);

    boolean hasApplied(Long jobId, Long candidateId);

    long countByJob(Long jobId);
}
