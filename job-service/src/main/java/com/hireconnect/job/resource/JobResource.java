package com.hireconnect.job.resource;

import com.hireconnect.job.entity.Job;
import com.hireconnect.job.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobResource {

    private final JobService jobService;

    @PostMapping
    public ResponseEntity<?> createJob(
            @RequestBody Job job,
            @RequestHeader(value = "X-User-Id",           defaultValue = "") String userId,
            @RequestHeader(value = "X-User-Role",         defaultValue = "") String role,
            @RequestHeader(value = "X-Subscription-Plan", defaultValue = "FREE") String plan,
            @RequestHeader(value = "X-Job-Limit",         defaultValue = "3")  String jobLimitHeader) {

        if (userId.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required to post a job."));
        }
        if (!"RECRUITER".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only recruiters can post jobs."));
        }

        job.setPostedBy(Long.parseLong(userId));

        int jobLimit;
        try {
            jobLimit = Integer.parseInt(jobLimitHeader);
        } catch (NumberFormatException e) {
            jobLimit = 3;
        }

        return ResponseEntity.ok(jobService.addJob(job, plan.toUpperCase(), jobLimit));
    }

    @GetMapping
    public ResponseEntity<List<Job>> getAllActiveJobs() {
        return ResponseEntity.ok(jobService.getAllActiveJobs());
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<Job> getJobById(@PathVariable Long jobId) {
        return ResponseEntity.ok(jobService.getJobById(jobId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Job>> searchJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Double salaryMin,
            @RequestParam(required = false) Double salaryMax,
            @RequestParam(required = false) Integer experience) {
        return ResponseEntity.ok(
                jobService.searchJobs(keyword, location, category, type,
                                      salaryMin, salaryMax, experience));
    }

    @GetMapping("/recruiter/{recruiterId}")
    public ResponseEntity<List<Job>> getByRecruiter(@PathVariable Long recruiterId) {
        return ResponseEntity.ok(jobService.getJobsByRecruiter(recruiterId));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Job>> getByCategory(@PathVariable String category) {
        return ResponseEntity.ok(jobService.getJobsByCategory(category));
    }

    @GetMapping("/location/{location}")
    public ResponseEntity<List<Job>> getByLocation(@PathVariable String location) {
        return ResponseEntity.ok(jobService.getJobsByLocation(location));
    }

    @PutMapping("/{jobId}")
    public ResponseEntity<Job> updateJob(
            @PathVariable Long jobId,
            @RequestBody Job job,
            @RequestHeader(value = "X-User-Id", defaultValue = "") String userId) {

        if (userId.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(null);
        }
        return ResponseEntity.ok(jobService.updateJob(jobId, job));
    }

    @PatchMapping("/{jobId}/status")
    public ResponseEntity<Job> updateStatus(
            @PathVariable Long jobId,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(jobService.updateJobStatus(jobId, body.get("status")));
    }

    @DeleteMapping("/{jobId}")
    public ResponseEntity<Map<String, String>> deleteJob(@PathVariable Long jobId) {
        jobService.deleteJob(jobId);
        return ResponseEntity.ok(Map.of("message", "Job deleted successfully"));
    }
}
