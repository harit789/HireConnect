package com.hireconnect.job.resource;

import com.hireconnect.job.entity.Application;
import com.hireconnect.job.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationResource {

    private final ApplicationService applicationService;

    @PostMapping
    public ResponseEntity<?> submit(
            @RequestBody Application application,
            @RequestHeader(value = "X-User-Id", defaultValue = "") String userId,
            @RequestHeader(value = "X-User-Role", defaultValue = "") String role) {

        if (userId.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required to apply for a job."));
        }
        if (!"CANDIDATE".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only candidates can apply for jobs."));
        }

        application.setCandidateId(Long.parseLong(userId));
        return ResponseEntity.ok(applicationService.submitApplication(application));
    }

    @GetMapping("/{applicationId}")
    public ResponseEntity<Application> getById(@PathVariable Long applicationId) {
        return ResponseEntity.ok(applicationService.getApplicationById(applicationId));
    }

    @GetMapping("/candidate/{candidateId}")
    public ResponseEntity<List<Application>> getByCandidate(@PathVariable Long candidateId) {
        return ResponseEntity.ok(applicationService.getByCandidate(candidateId));
    }

    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<Application>> getByJob(@PathVariable Long jobId) {
        return ResponseEntity.ok(applicationService.getByJob(jobId));
    }

    @GetMapping("/job/{jobId}/status/{status}")
    public ResponseEntity<List<Application>> getByJobAndStatus(
            @PathVariable Long jobId,
            @PathVariable String status) {
        return ResponseEntity.ok(applicationService.getByJobAndStatus(jobId, status));
    }

    @PatchMapping("/{applicationId}/status")
    public ResponseEntity<Application> updateStatus(
            @PathVariable Long applicationId,
            @RequestBody Map<String, String> body) {
        String status = body.get("status");
        String note   = body.get("recruiterNote");
        return ResponseEntity.ok(applicationService.updateStatus(applicationId, status, note));
    }

    @DeleteMapping("/{applicationId}/withdraw")
    public ResponseEntity<?> withdraw(
            @PathVariable Long applicationId,
            @RequestHeader(value = "X-User-Id", defaultValue = "") String userId) {

        if (userId.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required."));
        }
        applicationService.withdrawApplication(applicationId, Long.parseLong(userId));
        return ResponseEntity.ok(Map.of("message", "Application withdrawn successfully"));
    }

    @GetMapping("/check")
    public ResponseEntity<Map<String, Boolean>> hasApplied(
            @RequestParam Long jobId,
            @RequestParam Long candidateId) {
        return ResponseEntity.ok(
                Map.of("applied", applicationService.hasApplied(jobId, candidateId)));
    }

    @GetMapping("/job/{jobId}/count")
    public ResponseEntity<Map<String, Long>> countByJob(@PathVariable Long jobId) {
        return ResponseEntity.ok(
                Map.of("count", applicationService.countByJob(jobId)));
    }
}
