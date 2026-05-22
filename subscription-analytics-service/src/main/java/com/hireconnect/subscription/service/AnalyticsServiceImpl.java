package com.hireconnect.subscription.service;

import com.hireconnect.subscription.entity.AnalyticsSummary;
import com.hireconnect.subscription.repository.SubscriptionRepository;
import com.hireconnect.subscription.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsServiceImpl implements AnalyticsService {

    private final SubscriptionRepository subscriptionRepo;
    private final InvoiceRepository invoiceRepo;
    private final RestTemplate restTemplate;

    @Value("${services.job-service-url}")
    private String jobServiceUrl;

    @Override
    public AnalyticsSummary getRecruiterStats(Long recruiterId, String authToken) {

        List<Map<String, Object>> jobs = fetchList(
                jobServiceUrl + "/jobs/recruiter/" + recruiterId, authToken,
                recruiterId.toString(), "RECRUITER");

        int totalJobs   = jobs.size();
        int activeJobs  = (int) jobs.stream()
                .filter(j -> "ACTIVE".equals(j.get("status"))).count();
        int closedJobs  = (int) jobs.stream()
                .filter(j -> "CLOSED".equals(j.get("status"))).count();

        int totalApps = 0, shortlisted = 0, interviewScheduled = 0,
            offered = 0, rejected = 0;

        for (Map<String, Object> job : jobs) {
            Long jobId = toLong(job.get("jobId"));
            List<Map<String, Object>> apps = fetchList(
                    jobServiceUrl + "/applications/job/" + jobId, authToken,
                    recruiterId.toString(), "RECRUITER");
            totalApps += apps.size();
            for (Map<String, Object> app : apps) {
                String status = (String) app.get("status");
                if (status != null) {
                    switch (status) {
                        case "SHORTLISTED"          -> shortlisted++;
                        case "INTERVIEW_SCHEDULED"  -> interviewScheduled++;
                        case "OFFERED"              -> offered++;
                        case "REJECTED"             -> rejected++;
                    }
                }
            }
        }

        double viewToApplyRatio = totalJobs > 0
                ? Math.round((double) totalApps / totalJobs * 100.0) / 100.0 : 0.0;

        double offerRate = totalApps > 0
                ? Math.round((double) offered / totalApps * 10000.0) / 100.0 : 0.0;

        Map<String, Long> byStatus = new LinkedHashMap<>();
        byStatus.put("APPLIED",              (long)(totalApps - shortlisted - interviewScheduled - offered - rejected));
        byStatus.put("SHORTLISTED",          (long) shortlisted);
        byStatus.put("INTERVIEW_SCHEDULED",  (long) interviewScheduled);
        byStatus.put("OFFERED",              (long) offered);
        byStatus.put("REJECTED",             (long) rejected);

        return AnalyticsSummary.builder()
                .recruiterId(recruiterId)
                .totalJobsPosted(totalJobs)
                .activeJobs(activeJobs)
                .closedJobs(closedJobs)
                .totalApplications(totalApps)
                .shortlistedCount(shortlisted)
                .interviewScheduledCount(interviewScheduled)
                .offeredCount(offered)
                .rejectedCount(rejected)
                .viewToApplyRatio(viewToApplyRatio)
                .offerAcceptanceRate(offerRate)
                .applicationsByStatus(byStatus)
                .build();
    }

    @Override
    public AnalyticsSummary getPlatformStats(String authToken) {

        List<Map<String, Object>> allJobs = fetchList(
                jobServiceUrl + "/jobs", authToken, "0", "ADMIN");

        long totalJobs   = allJobs.size();
        long activeJobs  = allJobs.stream()
                .filter(j -> "ACTIVE".equals(j.get("status"))).count();

        Map<String, Long> jobsByCategory = allJobs.stream()
                .filter(j -> j.get("category") != null)
                .collect(Collectors.groupingBy(
                        j -> (String) j.get("category"),
                        Collectors.counting()
                ));

        long activeSubscriptions = subscriptionRepo.countByStatus("ACTIVE");
        Double totalRevenue      = invoiceRepo.getTotalPaidAmount();

        return AnalyticsSummary.builder()
                .totalJobsOnPlatform(totalJobs)
                .activeJobs((int) activeJobs)
                .jobsByCategory(jobsByCategory)
                .activeSubscriptions(activeSubscriptions)
                .totalRevenue(totalRevenue != null ? totalRevenue : 0.0)
                .build();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> fetchList(String url, String authToken, String userId, String role) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authToken);
            headers.set("X-User-Id", userId);
            headers.set("X-User-Role", role);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity,
                    new ParameterizedTypeReference<>() {});

            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (Exception e) {
            log.warn("Analytics fetch failed for {}: {}", url, e.getMessage());
            return Collections.emptyList();
        }
    }

    private Long toLong(Object value) {
        if (value == null) return 0L;
        if (value instanceof Number n) return n.longValue();
        try { return Long.parseLong(value.toString()); }
        catch (NumberFormatException e) { return 0L; }
    }
}
