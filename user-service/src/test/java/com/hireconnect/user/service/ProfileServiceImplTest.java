package com.hireconnect.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hireconnect.user.entity.CandidateProfile;
import com.hireconnect.user.entity.RecruiterProfile;
import com.hireconnect.user.repository.CandidateProfileRepository;
import com.hireconnect.user.repository.RecruiterProfileRepository;

@ExtendWith(MockitoExtension.class)
public class ProfileServiceImplTest {

    @Mock
    private CandidateProfileRepository candidateRepo;

    @Mock
    private RecruiterProfileRepository recruiterRepo;

    @InjectMocks
    private ProfileServiceImpl profileService;

    private CandidateProfile mockCandidate;
    private RecruiterProfile mockRecruiter;

    @BeforeEach
    void setUp() {
        mockCandidate = new CandidateProfile();
        mockCandidate.setProfileId(1L);
        mockCandidate.setUserId(1L);
        mockCandidate.setFullName("John Doe");
        mockCandidate.setSavedJobs(new ArrayList<>());

        mockRecruiter = new RecruiterProfile();
        mockRecruiter.setProfileId(2L);
        mockRecruiter.setUserId(2L);
        mockRecruiter.setCompanyName("Tech Corp");
    }

    @Test
    void addCandidateProfile_Success() {
        when(candidateRepo.save(any(CandidateProfile.class))).thenReturn(mockCandidate);

        CandidateProfile saved = profileService.addCandidateProfile(mockCandidate);

        assertNotNull(saved);
        assertEquals("John Doe", saved.getFullName());
        verify(candidateRepo, times(1)).save(mockCandidate);
    }

    @Test
    void addRecruiterProfile_Success() {
        when(recruiterRepo.save(any(RecruiterProfile.class))).thenReturn(mockRecruiter);

        RecruiterProfile saved = profileService.addRecruiterProfile(mockRecruiter);

        assertNotNull(saved);
        assertEquals("Tech Corp", saved.getCompanyName());
        // verify teamId is set to userId if null
        assertEquals(2L, mockRecruiter.getTeamId());
        verify(recruiterRepo, times(1)).save(mockRecruiter);
    }

    @Test
    void getCandidateByUserId_Success() {
        when(candidateRepo.findByUserId(1L)).thenReturn(Optional.of(mockCandidate));

        CandidateProfile found = profileService.getCandidateByUserId(1L);

        assertNotNull(found);
        assertEquals(1L, found.getUserId());
    }

    @Test
    void getCandidateByUserId_NotFound_ThrowsException() {
        when(candidateRepo.findByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> profileService.getCandidateByUserId(1L));
    }

    @Test
    void getRecruiterByUserId_Success() {
        when(recruiterRepo.findByUserId(2L)).thenReturn(Optional.of(mockRecruiter));

        RecruiterProfile found = profileService.getRecruiterByUserId(2L);

        assertNotNull(found);
        assertEquals(2L, found.getUserId());
    }

    @Test
    void bookmarkJob_Success() {
        when(candidateRepo.findByUserId(1L)).thenReturn(Optional.of(mockCandidate));
        when(candidateRepo.save(any(CandidateProfile.class))).thenReturn(mockCandidate);

        CandidateProfile updated = profileService.bookmarkJob(1L, 100L);

        assertNotNull(updated);
        assertTrue(updated.getSavedJobs().contains(100L));
    }

    @Test
    void removeBookmark_Success() {
        mockCandidate.getSavedJobs().add(100L);
        when(candidateRepo.findByUserId(1L)).thenReturn(Optional.of(mockCandidate));
        when(candidateRepo.save(any(CandidateProfile.class))).thenReturn(mockCandidate);

        CandidateProfile updated = profileService.removeBookmark(1L, 100L);

        assertNotNull(updated);
        assertFalse(updated.getSavedJobs().contains(100L));
    }

    @Test
    void joinTeam_Success() {
        when(recruiterRepo.findByUserId(2L)).thenReturn(Optional.of(mockRecruiter));
        when(recruiterRepo.save(any(RecruiterProfile.class))).thenReturn(mockRecruiter);

        RecruiterProfile updated = profileService.joinTeam(2L, 5L);

        assertNotNull(updated);
        assertEquals(5L, updated.getTeamId());
    }
}
