package com.hireconnect.job.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hireconnect.job.entity.Application;
import com.hireconnect.job.entity.Job;
import com.hireconnect.job.repository.ApplicationRepository;
import com.hireconnect.job.repository.JobRepository;
import com.hireconnect.job.dto.NotificationEvent;

@ExtendWith(MockitoExtension.class)
public class ApplicationServiceImplTest {

    @Mock
    private ApplicationRepository appRepo;

    @Mock
    private JobRepository jobRepo;

    @Mock
    private NotificationProducer notificationProducer;

    @InjectMocks
    private ApplicationServiceImpl applicationService;

    private Application mockApp;
    private Job mockJob;

    @BeforeEach
    void setUp() {
        mockJob = Job.builder().jobId(1L).status("ACTIVE").build();

        mockApp = new Application();
        mockApp.setApplicationId(10L);
        mockApp.setJobId(1L);
        mockApp.setCandidateId(100L);
        mockApp.setStatus("APPLIED");
    }

    @Test
    void submitApplication_Success() {
        when(jobRepo.findById(1L)).thenReturn(Optional.of(mockJob));
        when(appRepo.existsByJobIdAndCandidateId(1L, 100L)).thenReturn(false);
        when(appRepo.save(any(Application.class))).thenReturn(mockApp);

        Application saved = applicationService.submitApplication(mockApp);

        assertNotNull(saved);
        assertEquals("APPLIED", saved.getStatus());
        verify(appRepo, times(1)).save(mockApp);
    }

    @Test
    void submitApplication_JobNotActive_ThrowsException() {
        mockJob.setStatus("CLOSED");
        when(jobRepo.findById(1L)).thenReturn(Optional.of(mockJob));

        assertThrows(RuntimeException.class, () -> applicationService.submitApplication(mockApp));
    }

    @Test
    void submitApplication_AlreadyApplied_ThrowsException() {
        when(jobRepo.findById(1L)).thenReturn(Optional.of(mockJob));
        when(appRepo.existsByJobIdAndCandidateId(1L, 100L)).thenReturn(true);

        assertThrows(RuntimeException.class, () -> applicationService.submitApplication(mockApp));
    }

    @Test
    void getApplicationById_Success() {
        when(appRepo.findById(10L)).thenReturn(Optional.of(mockApp));

        Application found = applicationService.getApplicationById(10L);

        assertNotNull(found);
        assertEquals(10L, found.getApplicationId());
    }

    @Test
    void updateStatus_Success() {
        when(appRepo.findById(10L)).thenReturn(Optional.of(mockApp));
        when(appRepo.save(any(Application.class))).thenReturn(mockApp);

        Application updated = applicationService.updateStatus(10L, "SHORTLISTED", "Good profile");

        assertEquals("SHORTLISTED", updated.getStatus());
        assertEquals("Good profile", updated.getRecruiterNote());
        verify(notificationProducer, times(1)).sendNotification(any(NotificationEvent.class));
    }

    @Test
    void updateStatus_InvalidTransition_ThrowsException() {
        when(appRepo.findById(10L)).thenReturn(Optional.of(mockApp));

        assertThrows(RuntimeException.class, () -> applicationService.updateStatus(10L, "OFFERED", null));
        verify(appRepo, never()).save(any());
        verify(notificationProducer, never()).sendNotification(any());
    }

    @Test
    void withdrawApplication_Success() {
        when(appRepo.findById(10L)).thenReturn(Optional.of(mockApp));
        doNothing().when(appRepo).deleteById(10L);

        applicationService.withdrawApplication(10L, 100L);

        verify(appRepo, times(1)).deleteById(10L);
    }

    @Test
    void withdrawApplication_WrongCandidate_ThrowsException() {
        when(appRepo.findById(10L)).thenReturn(Optional.of(mockApp));

        assertThrows(RuntimeException.class, () -> applicationService.withdrawApplication(10L, 999L));
    }

    @Test
    void withdrawApplication_FinalizedStatus_ThrowsException() {
        mockApp.setStatus("REJECTED");
        when(appRepo.findById(10L)).thenReturn(Optional.of(mockApp));

        assertThrows(RuntimeException.class, () -> applicationService.withdrawApplication(10L, 100L));
    }
}
