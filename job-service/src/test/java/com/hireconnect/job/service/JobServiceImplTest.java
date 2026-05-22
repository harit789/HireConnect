package com.hireconnect.job.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import com.hireconnect.job.entity.Job;
import com.hireconnect.job.entity.JobDocument;
import com.hireconnect.job.repository.ApplicationRepository;
import com.hireconnect.job.repository.JobRepository;
import com.hireconnect.job.repository.JobSearchRepository;

@ExtendWith(MockitoExtension.class)
public class JobServiceImplTest {

    @Mock
    private JobRepository jobRepo;

    @Mock
    private ApplicationRepository appRepo;

    @Mock
    private JobSearchRepository searchRepo;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private JobServiceImpl jobService;

    private Job mockJob;

    @BeforeEach
    void setUp() {
        mockJob = Job.builder()
                .jobId(1L)
                .title("Software Engineer")
                .postedBy(100L)
                .status("ACTIVE")
                .build();
    }

    @Test
    void addJob_Success() {
        when(jobRepo.countByPostedBy(100L)).thenReturn(1L);
        when(jobRepo.save(any(Job.class))).thenReturn(mockJob);

        Job saved = jobService.addJob(mockJob, "FREE", 3);

        assertNotNull(saved);
        assertEquals("ACTIVE", saved.getStatus());
        verify(jobRepo, times(1)).save(mockJob);
        verify(searchRepo, times(1)).save(any(JobDocument.class));
    }

    @Test
    void addJob_LimitReached_ThrowsException() {
        when(jobRepo.countByPostedBy(100L)).thenReturn(3L);

        assertThrows(RuntimeException.class, () -> jobService.addJob(mockJob, "FREE", 3));
        verify(jobRepo, never()).save(any(Job.class));
    }

    @Test
    void getAllActiveJobs_Success() {
        when(jobRepo.findByStatus("ACTIVE")).thenReturn(Arrays.asList(mockJob));

        List<Job> jobs = jobService.getAllActiveJobs();

        assertFalse(jobs.isEmpty());
        assertEquals(1, jobs.size());
    }

    @Test
    void getJobById_Success() {
        when(jobRepo.findById(1L)).thenReturn(Optional.of(mockJob));

        Job found = jobService.getJobById(1L);

        assertNotNull(found);
        assertEquals(1L, found.getJobId());
    }

    @Test
    void getJobById_NotFound_ThrowsException() {
        when(jobRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> jobService.getJobById(1L));
    }

    @Test
    void searchJobs_WithKeyword_ElasticsearchSuccess() {
        JobDocument doc = JobDocument.builder().jobId(1L).title("Software Engineer").build();
        when(searchRepo.findByTitleContainingOrDescriptionContaining("Software", "Software"))
                .thenReturn(Arrays.asList(doc));
        when(jobRepo.findById(1L)).thenReturn(Optional.of(mockJob));

        List<Job> results = jobService.searchJobs("Software", null, null, null, null, null, null);

        assertFalse(results.isEmpty());
        assertEquals(1L, results.get(0).getJobId());
    }

    @Test
    void searchJobs_FallbackToJPA() {
        when(searchRepo.findByTitleContainingOrDescriptionContaining("Software", "Software"))
                .thenThrow(new RuntimeException("ES down"));
        when(jobRepo.searchJobs("Software", null, null, null, null, null, null))
                .thenReturn(Arrays.asList(mockJob));

        List<Job> results = jobService.searchJobs("Software", null, null, null, null, null, null);

        assertFalse(results.isEmpty());
        assertEquals(1L, results.get(0).getJobId());
    }

    @Test
    void updateJobStatus_Success() {
        when(jobRepo.findById(1L)).thenReturn(Optional.of(mockJob));
        when(jobRepo.save(any(Job.class))).thenReturn(mockJob);

        Job updated = jobService.updateJobStatus(1L, "CLOSED");

        assertNotNull(updated);
        assertEquals("CLOSED", mockJob.getStatus());
        verify(searchRepo, times(1)).save(any(JobDocument.class));
    }

    @Test
    void deleteJob_Success() {
        doNothing().when(appRepo).deleteByJobId(1L);
        doNothing().when(jobRepo).deleteById(1L);

        jobService.deleteJob(1L);

        verify(appRepo, times(1)).deleteByJobId(1L);
        verify(jobRepo, times(1)).deleteById(1L);
        verify(searchRepo, times(1)).deleteById("1");
    }
}
