package com.hireconnect.interview.service;

import com.hireconnect.interview.entity.Interview;

import java.util.List;

public interface InterviewService {

    Interview scheduleInterview(Interview interview);

    Interview getById(Long interviewId);

    List<Interview> getByApplication(Long applicationId);

    List<Interview> getByCandidate(Long candidateId);

    List<Interview> getByRecruiter(Long recruiterId);

    Interview confirmInterview(Long interviewId);

    Interview rescheduleInterview(Long interviewId, Interview updated);

    Interview cancelInterview(Long interviewId, Long requestedBy);

    Interview completeInterview(Long interviewId);
}
