package com.hireconnect.user.resource;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hireconnect.user.entity.CandidateProfile;
import com.hireconnect.user.entity.RecruiterProfile;
import com.hireconnect.user.service.ProfileService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileResource {

    private final ProfileService profileService;

    // ────────────────────────────────────────────────────────────────────────
    // Candidate endpoints
    // ────────────────────────────────────────────────────────────────────────

    /** POST /api/profile/candidate */
    @PostMapping("/candidate")
    public ResponseEntity<CandidateProfile> createCandidateProfile(
            @RequestBody CandidateProfile profile) {
        return ResponseEntity.ok(profileService.addCandidateProfile(profile));
    }

    /** GET /api/profile/candidate/user/{userId} */
    @GetMapping("/candidate/user/{userId}")
    public ResponseEntity<CandidateProfile> getCandidateByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(profileService.getCandidateByUserId(userId));
    }

    /** GET /api/profile/candidate/{profileId} */
    @GetMapping("/candidate/{profileId}")
    public ResponseEntity<CandidateProfile> getCandidateById(@PathVariable Long profileId) {
        return ResponseEntity.ok(profileService.getCandidateById(profileId));
    }

    /** PUT /api/profile/candidate/user/{userId} */
    @PutMapping("/candidate/user/{userId}")
    public ResponseEntity<CandidateProfile> updateCandidate(
            @PathVariable Long userId,
            @RequestBody CandidateProfile profile) {
        return ResponseEntity.ok(profileService.updateCandidateProfile(userId, profile));
    }

    /** POST /api/profile/candidate/user/{userId}/bookmark/{jobId} */
    @PostMapping("/candidate/user/{userId}/bookmark/{jobId}")
    public ResponseEntity<CandidateProfile> bookmarkJob(
            @PathVariable Long userId,
            @PathVariable Long jobId) {
        return ResponseEntity.ok(profileService.bookmarkJob(userId, jobId));
    }

    /** DELETE /api/profile/candidate/user/{userId}/bookmark/{jobId} */
    @DeleteMapping("/candidate/user/{userId}/bookmark/{jobId}")
    public ResponseEntity<CandidateProfile> removeBookmark(
            @PathVariable Long userId,
            @PathVariable Long jobId) {
        return ResponseEntity.ok(profileService.removeBookmark(userId, jobId));
    }

    /** POST /api/profile/candidate/user/{userId}/resume */
    @PostMapping("/candidate/user/{userId}/resume")
    public ResponseEntity<CandidateProfile> uploadResume(
            @PathVariable Long userId,
            @org.springframework.web.bind.annotation.RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        return ResponseEntity.ok(profileService.uploadResume(userId, file));
    }

    /** DELETE /api/profile/candidate/user/{userId} */
    @DeleteMapping("/candidate/user/{userId}")
    public ResponseEntity<Map<String, String>> deleteCandidate(@PathVariable Long userId) {
        profileService.deleteCandidateProfile(userId);
        return ResponseEntity.ok(Map.of("message", "Candidate profile deleted"));
    }

    /** GET /api/profile/candidates */
    @GetMapping("/candidates")
    public ResponseEntity<List<CandidateProfile>> getAllCandidates() {
        return ResponseEntity.ok(profileService.getAllCandidates());
    }

    // ────────────────────────────────────────────────────────────────────────
    // Recruiter endpoints
    // ────────────────────────────────────────────────────────────────────────

    /** POST /api/profile/recruiter */
    @PostMapping("/recruiter")
    public ResponseEntity<RecruiterProfile> createRecruiterProfile(
            @RequestBody RecruiterProfile profile) {
        return ResponseEntity.ok(profileService.addRecruiterProfile(profile));
    }

    /** GET /api/profile/recruiter/user/{userId} */
    @GetMapping("/recruiter/user/{userId}")
    public ResponseEntity<RecruiterProfile> getRecruiterByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(profileService.getRecruiterByUserId(userId));
    }

    /** GET /api/profile/recruiter/{profileId} */
    @GetMapping("/recruiter/{profileId}")
    public ResponseEntity<RecruiterProfile> getRecruiterById(@PathVariable Long profileId) {
        return ResponseEntity.ok(profileService.getRecruiterById(profileId));
    }

    /** PUT /api/profile/recruiter/user/{userId} */
    @PutMapping("/recruiter/user/{userId}")
    public ResponseEntity<RecruiterProfile> updateRecruiter(
            @PathVariable Long userId,
            @RequestBody RecruiterProfile profile) {
        return ResponseEntity.ok(profileService.updateRecruiterProfile(userId, profile));
    }

    /** DELETE /api/profile/recruiter/user/{userId} */
    @DeleteMapping("/recruiter/user/{userId}")
    public ResponseEntity<Map<String, String>> deleteRecruiter(@PathVariable Long userId) {
        profileService.deleteRecruiterProfile(userId);
        return ResponseEntity.ok(Map.of("message", "Recruiter profile deleted"));
    }

    /** GET /api/profile/recruiters */
    @GetMapping("/recruiters")
    public ResponseEntity<List<RecruiterProfile>> getAllRecruiters() {
        return ResponseEntity.ok(profileService.getAllRecruiters());
    }

    /** PATCH /api/profile/recruiter/user/{userId}/join-team/{teamId} */
    @PatchMapping("/recruiter/user/{userId}/join-team/{teamId}")
    public ResponseEntity<RecruiterProfile> joinTeam(
            @PathVariable Long userId,
            @PathVariable Long teamId) {
        return ResponseEntity.ok(profileService.joinTeam(userId, teamId));
    }
}
