package com.hireconnect.interview.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hireconnect.interview.entity.Interview;
import com.hireconnect.interview.repository.InterviewRepository;

@ExtendWith(MockitoExtension.class)
public class InterviewServiceImplTest {

    @Mock
    private InterviewRepository interviewRepo;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private InterviewServiceImpl interviewService;

    private Interview mockInterview;

    @BeforeEach
    void setUp() {
        mockInterview = new Interview();
        mockInterview.setInterviewId(1L);
        mockInterview.setApplicationId(10L);
        mockInterview.setCandidateId(100L);
        mockInterview.setRecruiterId(200L);
        mockInterview.setCandidateEmail("test@example.com");
        mockInterview.setScheduledAt(LocalDateTime.now().plusDays(1));
        mockInterview.setMode("ONLINE");
        mockInterview.setStatus("SCHEDULED");
    }

    @Test
    void scheduleInterview_Success() {
        when(interviewRepo.save(any(Interview.class))).thenReturn(mockInterview);

        Interview scheduled = interviewService.scheduleInterview(mockInterview);

        assertNotNull(scheduled);
        assertEquals("SCHEDULED", scheduled.getStatus());
        verify(interviewRepo, times(1)).save(any(Interview.class));
        verify(notificationService, times(1)).createNotification(
                eq(100L), eq("INTERVIEW_SCHEDULED"), anyString(), eq(1L), eq("INTERVIEW")
        );
        verify(notificationService, times(1)).sendEmailAsync(
                eq("test@example.com"), anyString(), anyString()
        );
    }

    @Test
    void getById_Success() {
        when(interviewRepo.findById(1L)).thenReturn(Optional.of(mockInterview));

        Interview found = interviewService.getById(1L);

        assertNotNull(found);
        assertEquals(1L, found.getInterviewId());
    }

    @Test
    void getById_NotFound_ThrowsException() {
        when(interviewRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> interviewService.getById(1L));
    }

    @Test
    void confirmInterview_Success() {
        when(interviewRepo.findById(1L)).thenReturn(Optional.of(mockInterview));
        when(interviewRepo.save(any(Interview.class))).thenReturn(mockInterview);

        Interview confirmed = interviewService.confirmInterview(1L);

        assertEquals("CONFIRMED", confirmed.getStatus());
        verify(notificationService, times(1)).createNotification(
                eq(200L), eq("INTERVIEW_UPDATED"), anyString(), eq(1L), eq("INTERVIEW")
        );
    }

    @Test
    void confirmInterview_InvalidStatus_ThrowsException() {
        mockInterview.setStatus("CANCELLED");
        when(interviewRepo.findById(1L)).thenReturn(Optional.of(mockInterview));

        assertThrows(RuntimeException.class, () -> interviewService.confirmInterview(1L));
    }

    @Test
    void rescheduleInterview_Success() {
        when(interviewRepo.findById(1L)).thenReturn(Optional.of(mockInterview));
        when(interviewRepo.save(any(Interview.class))).thenReturn(mockInterview);

        Interview updated = new Interview();
        updated.setScheduledAt(LocalDateTime.now().plusDays(2));
        updated.setMode("OFFLINE");

        Interview rescheduled = interviewService.rescheduleInterview(1L, updated);

        assertEquals("RESCHEDULED", rescheduled.getStatus());
        verify(notificationService, times(1)).createNotification(
                eq(100L), anyString(), anyString(), eq(1L), eq("INTERVIEW")
        );
        verify(notificationService, times(1)).createNotification(
                eq(200L), anyString(), anyString(), eq(1L), eq("INTERVIEW")
        );
    }

    @Test
    void cancelInterview_Success() {
        when(interviewRepo.findById(1L)).thenReturn(Optional.of(mockInterview));
        when(interviewRepo.save(any(Interview.class))).thenReturn(mockInterview);

        Interview cancelled = interviewService.cancelInterview(1L, 100L);

        assertEquals("CANCELLED", cancelled.getStatus());

        verify(notificationService, times(1)).createNotification(
                eq(200L), eq("INTERVIEW_CANCELLED"), anyString(), eq(1L), eq("INTERVIEW")
        );
    }

    @Test
    void completeInterview_Success() {
        when(interviewRepo.findById(1L)).thenReturn(Optional.of(mockInterview));
        when(interviewRepo.save(any(Interview.class))).thenReturn(mockInterview);

        Interview completed = interviewService.completeInterview(1L);

        assertEquals("COMPLETED", completed.getStatus());
    }
}
