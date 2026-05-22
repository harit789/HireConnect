package com.hireconnect.subscription.resource;

import com.hireconnect.subscription.entity.AnalyticsSummary;
import com.hireconnect.subscription.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsResource {

    private final AnalyticsService analyticsService;

    @GetMapping("/recruiter/{recruiterId}")
    public ResponseEntity<?> getRecruiterStats(
            @PathVariable Long recruiterId,
            @RequestHeader(value = "Authorization", defaultValue = "") String authToken) {

        if (authToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Authorization header required");
        }
        return ResponseEntity.ok(analyticsService.getRecruiterStats(recruiterId, authToken));
    }

    @GetMapping("/admin")
    public ResponseEntity<?> getPlatformStats(
            @RequestHeader(value = "Authorization", defaultValue = "") String authToken) {

        if (authToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Authorization header required");
        }
        return ResponseEntity.ok(analyticsService.getPlatformStats(authToken));
    }

    @GetMapping("/admin/export")
    public ResponseEntity<String> exportReport(
            @RequestHeader(value = "Authorization", defaultValue = "") String authToken) {

        if (authToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorization header required");
        }

        AnalyticsSummary stats = analyticsService.getPlatformStats(authToken);

        String csv = "Total Jobs,Total Applications,Shortlisted,Offered,Rejected,Avg Time To Hire,View To Apply Ratio\n" +
                     stats.getTotalJobsOnPlatform() + "," +
                     stats.getTotalApplicationsOnPlatform() + "," +
                     stats.getShortlistedCount() + "," +
                     stats.getOfferedCount() + "," +
                     stats.getRejectedCount() + "," +
                     stats.getAvgTimeToHireDays() + "," +
                     stats.getViewToApplyRatio();

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=platform_report.csv")
                .header("Content-Type", "text/csv")
                .body(csv);
    }
}
