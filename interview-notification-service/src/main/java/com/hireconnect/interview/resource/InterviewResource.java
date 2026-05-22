package com.hireconnect.interview.resource;

import com.hireconnect.interview.entity.Interview;
import com.hireconnect.interview.service.InterviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/interviews")
@RequiredArgsConstructor
public class InterviewResource {

    private final InterviewService interviewService;

    @PostMapping
    public ResponseEntity<Interview> schedule(@RequestBody Interview interview) {
        return ResponseEntity.ok(interviewService.scheduleInterview(interview));
    }

    @GetMapping("/{interviewId}")
    public ResponseEntity<Interview> getById(@PathVariable Long interviewId) {
        return ResponseEntity.ok(interviewService.getById(interviewId));
    }

    @GetMapping("/application/{applicationId}")
    public ResponseEntity<List<Interview>> getByApplication(
            @PathVariable Long applicationId) {
        return ResponseEntity.ok(interviewService.getByApplication(applicationId));
    }

    @GetMapping("/candidate/{candidateId}")
    public ResponseEntity<List<Interview>> getByCandidate(
            @PathVariable Long candidateId) {
        return ResponseEntity.ok(interviewService.getByCandidate(candidateId));
    }

    @GetMapping("/recruiter/{recruiterId}")
    public ResponseEntity<List<Interview>> getByRecruiter(
            @PathVariable Long recruiterId) {
        return ResponseEntity.ok(interviewService.getByRecruiter(recruiterId));
    }

    @PatchMapping("/{interviewId}/confirm")
    public ResponseEntity<Interview> confirm(@PathVariable Long interviewId) {
        return ResponseEntity.ok(interviewService.confirmInterview(interviewId));
    }

    @PutMapping("/{interviewId}/reschedule")
    public ResponseEntity<Interview> reschedule(
            @PathVariable Long interviewId,
            @RequestBody Interview updated) {
        return ResponseEntity.ok(interviewService.rescheduleInterview(interviewId, updated));
    }

    @PatchMapping("/{interviewId}/cancel")
    public ResponseEntity<Interview> cancel(
            @PathVariable Long interviewId,
            @RequestParam Long requestedBy) {
        return ResponseEntity.ok(interviewService.cancelInterview(interviewId, requestedBy));
    }

    @PatchMapping("/{interviewId}/complete")
    public ResponseEntity<Interview> complete(@PathVariable Long interviewId) {
        return ResponseEntity.ok(interviewService.completeInterview(interviewId));
    }
}
