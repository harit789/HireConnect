package com.hireconnect.interview.service;

import com.hireconnect.interview.entity.Interview;
import com.hireconnect.interview.repository.InterviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InterviewServiceImpl implements InterviewService {

    private final InterviewRepository interviewRepo;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public Interview scheduleInterview(Interview interview) {
        interview.setStatus("SCHEDULED");
        Interview saved = interviewRepo.save(interview);

        notificationService.createNotification(
                saved.getCandidateId(),
                "INTERVIEW_SCHEDULED",
                "Your interview has been scheduled for " + saved.getScheduledAt()
                        + ". Mode: " + saved.getMode(),
                saved.getInterviewId(),
                "INTERVIEW"
        );

        if (saved.getCandidateEmail() != null) {
            notificationService.sendEmailAsync(
                    saved.getCandidateEmail(),
                    "Interview Scheduled — HireConnect",
                    buildInterviewEmail(saved)
            );
        }

        return saved;
    }

    @Override
    public Interview getById(Long interviewId) {
        return interviewRepo.findById(interviewId)
                .orElseThrow(() -> new RuntimeException("Interview not found: " + interviewId));
    }

    @Override
    public List<Interview> getByApplication(Long applicationId) {
        return interviewRepo.findByApplicationId(applicationId);
    }

    @Override
    public List<Interview> getByCandidate(Long candidateId) {
        return interviewRepo.findByCandidateId(candidateId);
    }

    @Override
    public List<Interview> getByRecruiter(Long recruiterId) {
        return interviewRepo.findByRecruiterId(recruiterId);
    }

    @Override
    @Transactional
    public Interview confirmInterview(Long interviewId) {
        Interview interview = getById(interviewId);
        if (!"SCHEDULED".equals(interview.getStatus())
                && !"RESCHEDULED".equals(interview.getStatus())) {
            throw new RuntimeException("Only SCHEDULED or RESCHEDULED interviews can be confirmed.");
        }
        interview.setStatus("CONFIRMED");
        Interview saved = interviewRepo.save(interview);

        notificationService.createNotification(
                saved.getRecruiterId(),
                "INTERVIEW_UPDATED",
                "Candidate confirmed the interview scheduled for " + saved.getScheduledAt(),
                saved.getInterviewId(),
                "INTERVIEW"
        );

        return saved;
    }

    @Override
    @Transactional
    public Interview rescheduleInterview(Long interviewId, Interview updated) {
        Interview existing = getById(interviewId);
        if ("CANCELLED".equals(existing.getStatus())
                || "COMPLETED".equals(existing.getStatus())) {
            throw new RuntimeException("Cannot reschedule a " + existing.getStatus() + " interview.");
        }

        existing.setScheduledAt(updated.getScheduledAt());
        existing.setMode(updated.getMode());
        existing.setMeetLink(updated.getMeetLink());
        existing.setLocation(updated.getLocation());
        existing.setNotes(updated.getNotes());
        existing.setStatus("RESCHEDULED");
        Interview saved = interviewRepo.save(existing);

        String msg = "Interview rescheduled to " + saved.getScheduledAt();
        notificationService.createNotification(
                saved.getCandidateId(), "INTERVIEW_UPDATED", msg,
                saved.getInterviewId(), "INTERVIEW");
        notificationService.createNotification(
                saved.getRecruiterId(), "INTERVIEW_UPDATED", msg,
                saved.getInterviewId(), "INTERVIEW");

        if (saved.getCandidateEmail() != null) {
            notificationService.sendEmailAsync(
                    saved.getCandidateEmail(),
                    "Interview Rescheduled — HireConnect",
                    buildInterviewEmail(saved)
            );
        }

        return saved;
    }

    @Override
    @Transactional
    public Interview cancelInterview(Long interviewId, Long requestedBy) {
        Interview interview = getById(interviewId);
        if ("COMPLETED".equals(interview.getStatus())) {
            throw new RuntimeException("Cannot cancel a completed interview.");
        }
        interview.setStatus("CANCELLED");
        Interview saved = interviewRepo.save(interview);

        Long notifyUserId = requestedBy.equals(saved.getCandidateId())
                ? saved.getRecruiterId() : saved.getCandidateId();

        notificationService.createNotification(
                notifyUserId,
                "INTERVIEW_CANCELLED",
                "The interview scheduled for " + saved.getScheduledAt() + " has been cancelled.",
                saved.getInterviewId(),
                "INTERVIEW"
        );

        return saved;
    }

    @Override
    @Transactional
    public Interview completeInterview(Long interviewId) {
        Interview interview = getById(interviewId);
        interview.setStatus("COMPLETED");
        return interviewRepo.save(interview);
    }

    private String buildInterviewEmail(Interview i) {
        return "Dear Candidate,\n\n"
                + "Your interview details:\n"
                + "  Date & Time : " + i.getScheduledAt() + "\n"
                + "  Mode        : " + i.getMode() + "\n"
                + (i.getMeetLink() != null  ? "  Meet Link   : " + i.getMeetLink() + "\n" : "")
                + (i.getLocation()  != null ? "  Location    : " + i.getLocation() + "\n" : "")
                + (i.getNotes()     != null ? "  Notes       : " + i.getNotes() + "\n" : "")
                + "\nStatus: " + i.getStatus()
                + "\n\nBest regards,\nHireConnect Team";
    }
}
