package com.hireconnect.subscription.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import com.hireconnect.subscription.entity.AnalyticsSummary;
import com.hireconnect.subscription.repository.InvoiceRepository;
import com.hireconnect.subscription.repository.SubscriptionRepository;

@ExtendWith(MockitoExtension.class)
public class AnalyticsServiceImplTest {

    @Mock
    private SubscriptionRepository subscriptionRepo;

    @Mock
    private InvoiceRepository invoiceRepo;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AnalyticsServiceImpl analyticsService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(analyticsService, "jobServiceUrl", "http://job-service");
    }

    @Test
    void getRecruiterStats_Success() {

        List<Map<String, Object>> mockJobs = new ArrayList<>();
        Map<String, Object> job1 = new HashMap<>();
        job1.put("jobId", 1L);
        job1.put("status", "ACTIVE");
        mockJobs.add(job1);

        ResponseEntity<List<Map<String, Object>>> jobsResponse = ResponseEntity.ok(mockJobs);
        when(restTemplate.exchange(
                eq("http://job-service/jobs/recruiter/10"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(jobsResponse);

        List<Map<String, Object>> mockApps = new ArrayList<>();
        Map<String, Object> app1 = new HashMap<>();
        app1.put("applicationId", 100L);
        app1.put("status", "SHORTLISTED");
        mockApps.add(app1);

        ResponseEntity<List<Map<String, Object>>> appsResponse = ResponseEntity.ok(mockApps);
        when(restTemplate.exchange(
                eq("http://job-service/applications/job/1"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(appsResponse);

        AnalyticsSummary summary = analyticsService.getRecruiterStats(10L, "Bearer token");

        assertNotNull(summary);
        assertEquals(1, summary.getTotalJobsPosted());
        assertEquals(1, summary.getActiveJobs());
        assertEquals(1, summary.getTotalApplications());
        assertEquals(1, summary.getShortlistedCount());
        assertEquals(0, summary.getApplicationsByStatus().get("APPLIED"));
    }

    @Test
    void getPlatformStats_Success() {

        List<Map<String, Object>> mockJobs = new ArrayList<>();
        Map<String, Object> job1 = new HashMap<>();
        job1.put("jobId", 1L);
        job1.put("status", "ACTIVE");
        job1.put("category", "IT");
        mockJobs.add(job1);

        ResponseEntity<List<Map<String, Object>>> jobsResponse = ResponseEntity.ok(mockJobs);
        when(restTemplate.exchange(
                eq("http://job-service/jobs"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(jobsResponse);

        when(subscriptionRepo.countByStatus("ACTIVE")).thenReturn(50L);
        when(invoiceRepo.getTotalPaidAmount()).thenReturn(5000.0);

        AnalyticsSummary summary = analyticsService.getPlatformStats("Bearer token");

        assertNotNull(summary);
        assertEquals(1, summary.getTotalJobsOnPlatform());
        assertEquals(1, summary.getActiveJobs());
        assertEquals(1, summary.getJobsByCategory().get("IT"));
        assertEquals(50L, summary.getActiveSubscriptions());
        assertEquals(5000.0, summary.getTotalRevenue());
    }

    @Test
    void getRecruiterStats_ExceptionHandled() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenThrow(new RuntimeException("Service down"));

        AnalyticsSummary summary = analyticsService.getRecruiterStats(10L, "Bearer token");

        assertNotNull(summary);
        assertEquals(0, summary.getTotalJobsPosted());
    }
}
