package com.hireconnect.job.service;

import com.hireconnect.job.entity.Application;
import com.hireconnect.job.entity.Job;
import com.hireconnect.job.repository.ApplicationRepository;
import com.hireconnect.job.repository.JobRepository;
import com.hireconnect.job.dto.NotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository appRepo;
    private final JobRepository jobRepo;
    private final NotificationProducer notificationProducer;

    @Override
    @Transactional
    public Application submitApplication(Application application) {

        Job job = jobRepo.findById(application.getJobId())
                .filter(j -> "ACTIVE".equals(j.getStatus()))
                .orElseThrow(() -> new RuntimeException(
                        "Job not found or not accepting applications: " + application.getJobId()));

        if (appRepo.existsByJobIdAndCandidateId(
                application.getJobId(), application.getCandidateId())) {
            throw new RuntimeException("You have already applied to this job.");
        }

        application.setStatus("APPLIED");
        Application savedApp = appRepo.save(application);

        notificationProducer.sendNotification(NotificationEvent.builder()
                .userId(job.getPostedBy())
                .userEmail("")
                .subject("New Application Received")
                .message("A new application has been received for your job \""
                        + job.getTitle() + "\". Candidate ID: " + application.getCandidateId())
                .type("IN_APP")
                .referenceId(savedApp.getApplicationId())
                .referenceType("APPLICATION")
                .build());

        return savedApp;
    }

    @Override
    public Application getApplicationById(Long applicationId) {
        return appRepo.findById(applicationId)
                .orElseThrow(() -> new RuntimeException(
                        "Application not found: " + applicationId));
    }

    @Override
    public List<Application> getByCandidate(Long candidateId) {
        return appRepo.findByCandidateId(candidateId);
    }

    @Override
    public List<Application> getByJob(Long jobId) {
        return appRepo.findByJobId(jobId);
    }

    @Override
    public List<Application> getByJobAndStatus(Long jobId, String status) {
        return appRepo.findByJobIdAndStatus(jobId, status.toUpperCase());
    }

    @Override
    @Transactional
    public Application updateStatus(Long applicationId, String status, String recruiterNote) {
        Application app = getApplicationById(applicationId);

        validateStatusTransition(app.getStatus(), status.toUpperCase());

        app.setStatus(status.toUpperCase());
        if (recruiterNote != null && !recruiterNote.isBlank()) {
            app.setRecruiterNote(recruiterNote);
        }
        Application savedApp = appRepo.save(app);

        String jobTitle = jobRepo.findById(app.getJobId())
                .map(Job::getTitle)
                .orElse("Job #" + app.getJobId());

        String candidateMessage = "Your application for \"" + jobTitle
                + "\" has been updated to: " + status.toUpperCase();
        if (recruiterNote != null && !recruiterNote.isBlank()) {
            candidateMessage += ". Recruiter note: " + recruiterNote;
        }

        notificationProducer.sendNotification(NotificationEvent.builder()
                .userId(app.getCandidateId())
                .userEmail("")
                .subject("Application Status Update")
                .message(candidateMessage)
                .type("IN_APP")
                .referenceId(savedApp.getApplicationId())
                .referenceType("APPLICATION")
                .build());

        return savedApp;
    }

    @Override
    @Transactional
    public void withdrawApplication(Long applicationId, Long candidateId) {
        Application app = getApplicationById(applicationId);
        if (!app.getCandidateId().equals(candidateId)) {
            throw new RuntimeException("Unauthorized: You can only withdraw your own application.");
        }
        if ("OFFERED".equals(app.getStatus()) || "REJECTED".equals(app.getStatus())) {
            throw new RuntimeException("Cannot withdraw a finalized application.");
        }
        appRepo.deleteById(applicationId);
    }

    @Override
    public boolean hasApplied(Long jobId, Long candidateId) {
        return appRepo.existsByJobIdAndCandidateId(jobId, candidateId);
    }

    @Override
    public long countByJob(Long jobId) {
        return appRepo.countByJobId(jobId);
    }

    private void validateStatusTransition(String current, String next) {
        boolean valid = switch (current) {
            case "APPLIED"            -> next.equals("SHORTLISTED")         || next.equals("REJECTED");
            case "SHORTLISTED"        -> next.equals("INTERVIEW_SCHEDULED") || next.equals("REJECTED");
            case "INTERVIEW_SCHEDULED"-> next.equals("OFFERED")             || next.equals("REJECTED");
            default -> false;
        };
        if (!valid) {
            throw new RuntimeException(
                    "Invalid status transition: " + current + " -> " + next);
        }
    }
}
