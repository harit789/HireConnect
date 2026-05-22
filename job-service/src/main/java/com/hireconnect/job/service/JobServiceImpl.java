package com.hireconnect.job.service;

import com.hireconnect.job.entity.Job;
import com.hireconnect.job.entity.JobDocument;
import com.hireconnect.job.repository.ApplicationRepository;
import com.hireconnect.job.repository.JobRepository;
import com.hireconnect.job.repository.JobSearchRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Caching;

import java.util.List;

@Slf4j
@Service
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepo;
    private final ApplicationRepository appRepo;
    private final JobSearchRepository searchRepo;
    private final org.springframework.web.client.RestTemplate restTemplate;

    @org.springframework.beans.factory.annotation.Autowired
    public JobServiceImpl(JobRepository jobRepo,
                          ApplicationRepository appRepo,
                          @org.springframework.beans.factory.annotation.Autowired(required = false) JobSearchRepository searchRepo,
                          org.springframework.web.client.RestTemplate restTemplate) {
        this.jobRepo = jobRepo;
        this.appRepo = appRepo;
        this.searchRepo = searchRepo;
        this.restTemplate = restTemplate;
        if (searchRepo == null) {
            log.warn("Elasticsearch is unavailable — full-text search will fall back to JPA queries");
        }
    }

    private JobDocument convertToDocument(Job job) {
        return JobDocument.builder()
                .id(job.getJobId() != null ? String.valueOf(job.getJobId()) : null)
                .jobId(job.getJobId())
                .title(job.getTitle())
                .category(job.getCategory())
                .type(job.getType())
                .location(job.getLocation())
                .salaryMin(job.getSalaryMin())
                .salaryMax(job.getSalaryMax())
                .description(job.getDescription())
                .skills(job.getSkills())
                .experienceRequired(job.getExperienceRequired())
                .postedBy(job.getPostedBy())
                .status(job.getStatus())
                .build();
    }

    private void syncToElasticsearch(Job job) {
        if (searchRepo != null) {
            try {
                searchRepo.save(convertToDocument(job));
            } catch (Exception e) {
                log.warn("Failed to sync job {} to Elasticsearch: {}", job.getJobId(), e.getMessage());
            }
        }
    }

    private void deleteFromElasticsearch(Long jobId) {
        if (searchRepo != null) {
            try {
                searchRepo.deleteById(String.valueOf(jobId));
            } catch (Exception e) {
                log.warn("Failed to delete job {} from Elasticsearch: {}", jobId, e.getMessage());
            }
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "jobsList", allEntries = true)
    public Job addJob(Job job, String planName, int jobLimit) {

        int effectiveLimit = jobLimit;
        String effectivePlan = planName;

        try {
            String subUrl = "http://subscription-analytics-service/api/subscriptions/recruiter/" + job.getPostedBy() + "/active";
            java.util.Map<String, Object> sub = restTemplate.getForObject(subUrl, java.util.Map.class);
            if (sub != null && sub.containsKey("plan")) {
                effectivePlan = (String) sub.get("plan");
                String infoUrl = "http://subscription-analytics-service/api/subscriptions/plan-info/" + effectivePlan;
                java.util.Map<String, Object> info = restTemplate.getForObject(infoUrl, java.util.Map.class);
                if (info != null && info.containsKey("jobPostLimit")) {
                    Object limitObj = info.get("jobPostLimit");
                    if ("Unlimited".equals(limitObj)) {
                        effectiveLimit = -1;
                    } else {
                        effectiveLimit = (Integer) limitObj;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to fetch plan info for recruiter {}, using provided/default limits: {}", job.getPostedBy(), e.getMessage());
        }

        if (effectiveLimit != -1) {
            long currentCount = jobRepo.countByPostedBy(job.getPostedBy());
            if (currentCount >= effectiveLimit) {
                throw new RuntimeException(
                    "Job post limit reached for your " + effectivePlan + " plan (" +
                    effectiveLimit + " job" + (effectiveLimit == 1 ? "" : "s") +
                    " allowed). Please upgrade your subscription to post more jobs."
                );
            }
        }

        job.setStatus("ACTIVE");
        Job saved = jobRepo.save(job);
        syncToElasticsearch(saved);
        return saved;
    }

    @Override
    @Cacheable(value = "jobsList", key = "'active'")
    public List<Job> getAllActiveJobs() {
        return jobRepo.findByStatus("ACTIVE");
    }

    @Override
    @Cacheable(value = "jobs", key = "#jobId")
    public Job getJobById(Long jobId) {
        return jobRepo.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found: " + jobId));
    }

    @Override
    public List<Job> searchJobs(String keyword, String location, String category,
                                String type, Double salaryMin, Double salaryMax,
                                Integer experience) {

        if (keyword != null && !keyword.isEmpty() && searchRepo != null) {
            try {
                List<JobDocument> docs = searchRepo.findByTitleContainingOrDescriptionContaining(keyword, keyword);
                return docs.stream().map(d -> getJobById(d.getJobId())).toList();
            } catch (Exception e) {
                log.warn("Elasticsearch search failed, falling back to JPA: {}", e.getMessage());
            }
        }

        return jobRepo.searchJobs(keyword, location, category, type,
                                  salaryMin, salaryMax, experience);
    }

    @Override
    public List<Job> getJobsByRecruiter(Long recruiterId) {
        return jobRepo.findByPostedBy(recruiterId);
    }

    @Override
    public List<Job> getJobsByCategory(String category) {
        return jobRepo.findByCategory(category);
    }

    @Override
    public List<Job> getJobsByLocation(String location) {
        return jobRepo.findByLocation(location);
    }

    @Override
    @Transactional
    @Caching(
        put = { @CachePut(value = "jobs", key = "#jobId") },
        evict = { @CacheEvict(value = "jobsList", allEntries = true) }
    )
    public Job updateJob(Long jobId, Job updated) {
        Job existing = getJobById(jobId);
        updated.setJobId(existing.getJobId());
        updated.setPostedBy(existing.getPostedBy());
        updated.setPostedAt(existing.getPostedAt());
        Job saved = jobRepo.save(updated);
        syncToElasticsearch(saved);
        return saved;
    }

    @Override
    @Transactional
    @Caching(
        put = { @CachePut(value = "jobs", key = "#jobId") },
        evict = { @CacheEvict(value = "jobsList", allEntries = true) }
    )
    public Job updateJobStatus(Long jobId, String status) {
        Job job = getJobById(jobId);
        job.setStatus(status.toUpperCase());
        Job saved = jobRepo.save(job);
        syncToElasticsearch(saved);
        return saved;
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "jobs", key = "#jobId"),
        @CacheEvict(value = "jobsList", allEntries = true)
    })
    public void deleteJob(Long jobId) {

        appRepo.deleteByJobId(jobId);
        jobRepo.deleteById(jobId);
        deleteFromElasticsearch(jobId);
    }
}
