package com.hireconnect.job.service;

import com.hireconnect.job.entity.Job;

import java.util.List;

public interface JobService {

    Job addJob(Job job, String planName, int jobLimit);

    List<Job> getAllActiveJobs();

    Job getJobById(Long jobId);

    List<Job> searchJobs(String keyword, String location, String category,
                         String type, Double salaryMin, Double salaryMax,
                         Integer experience);

    List<Job> getJobsByRecruiter(Long recruiterId);

    List<Job> getJobsByCategory(String category);

    List<Job> getJobsByLocation(String location);

    Job updateJob(Long jobId, Job updated);

    Job updateJobStatus(Long jobId, String status);

    void deleteJob(Long jobId);
}
