package com.hireconnect.subscription.service;

import com.hireconnect.subscription.entity.AnalyticsSummary;

public interface AnalyticsService {

    AnalyticsSummary getRecruiterStats(Long recruiterId, String authToken);

    AnalyticsSummary getPlatformStats(String authToken);
}
