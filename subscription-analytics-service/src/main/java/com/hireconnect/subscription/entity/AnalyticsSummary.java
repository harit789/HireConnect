package com.hireconnect.subscription.entity;

import lombok.*;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsSummary {

    private Long recruiterId;
    private int totalJobsPosted;
    private int activeJobs;
    private int closedJobs;

    private int totalApplications;
    private int shortlistedCount;
    private int interviewScheduledCount;
    private int offeredCount;
    private int rejectedCount;

    private double viewToApplyRatio;
    private double avgTimeToHireDays;
    private double offerAcceptanceRate;

    private Map<String, Long> applicationsByStatus;
    private Map<String, Long> jobsByCategory;

    private Long totalCandidates;
    private Long totalRecruiters;
    private Long totalJobsOnPlatform;
    private Long totalApplicationsOnPlatform;
    private Long activeSubscriptions;
    private Double totalRevenue;
}
